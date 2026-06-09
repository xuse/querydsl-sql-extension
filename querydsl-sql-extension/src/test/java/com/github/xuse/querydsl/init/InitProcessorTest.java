package com.github.xuse.querydsl.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.ddl.CreateTableQuery;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * Unit tests for {@link InitProcessor} verifying task polling/execution
 * and stopOnError behavior.
 *
 * Requirements: 11.4, 11.5
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("InitProcessor - task execution and error handling")
class InitProcessorTest {

    @Mock
    private SQLQueryFactory factory;

    @Mock
    private SQLMetadataQueryFactory metadataFactory;

    @Mock
    private RelationalPathEx<?> mockTable;

    @Mock
    private CreateTableQuery createTableQuery;

    private ConfigurationEx configuration;
    private ScanOptions scanOptions;

    @BeforeEach
    void setup() throws Exception {
        // Use a real ConfigurationEx with H2Templates
        configuration = new ConfigurationEx(com.querydsl.sql.H2Templates.builder().build());
        scanOptions = configuration.getScanOptions();
        // Disable DDL permission detection to simplify test
        scanOptions.detectPermissions(false);
        // Disable distributed lock usage
        scanOptions.useDistributedLock(false);

        // Wire up the mock factory
        when(factory.getConfiguration()).thenReturn(configuration);
        when(factory.getMetadataFactory()).thenReturn(metadataFactory);
    }

    /**
     * Access the package-private initTasks queue via reflection.
     */
    @SuppressWarnings("unchecked")
    private BlockingQueue<TableInitTask> getInitTasksQueue() throws Exception {
        Field field = ConfigurationEx.class.getDeclaredField("initTasks");
        field.setAccessible(true);
        return (BlockingQueue<TableInitTask>) field.get(configuration);
    }

    @Nested
    @DisplayName("Task polling and execution")
    class TaskPollingAndExecution {

        @Test
        @DisplayName("queued TableInitTask is polled and executed - table does not exist, creates it")
        void testQueuedTaskIsPolledAndExecuted() throws Exception {
            // Arrange: queue a task
            SchemaAndTable schemaAndTable = new SchemaAndTable(null, "test_table");
            when(mockTable.getSchemaAndTable()).thenReturn(schemaAndTable);
            when(mockTable.getInitializeData()).thenReturn(null);

            TableInitTask task = new TableInitTask(mockTable);
            getInitTasksQueue().offer(task);

            // Mock: table does not exist, so it should be created
            when(metadataFactory.existsTable(any(SchemaAndTable.class), isNull())).thenReturn(false);
            doReturn(createTableQuery).when(metadataFactory).createTable(any(RelationalPath.class));
            when(createTableQuery.ifExists()).thenReturn(createTableQuery);

            // Act
            InitProcessor processor = new InitProcessor(factory, scanOptions);
            processor.run();

            // Assert: createTable was called for the task
            verify(metadataFactory, times(1)).existsTable(schemaAndTable, null);
            verify(metadataFactory, times(1)).createTable(mockTable);
            verify(createTableQuery, times(1)).ifExists();
            verify(createTableQuery, times(1)).execute();
        }

        @Test
        @DisplayName("multiple queued tasks are all polled and executed")
        void testMultipleQueuedTasksExecuted() throws Exception {
            // Arrange: queue two tasks
            RelationalPathEx<?> mockTable2 = mock(RelationalPathEx.class);
            SchemaAndTable sat1 = new SchemaAndTable(null, "table_1");
            SchemaAndTable sat2 = new SchemaAndTable(null, "table_2");

            when(mockTable.getSchemaAndTable()).thenReturn(sat1);
            when(mockTable.getInitializeData()).thenReturn(null);
            when(mockTable2.getSchemaAndTable()).thenReturn(sat2);
            when(mockTable2.getInitializeData()).thenReturn(null);

            BlockingQueue<TableInitTask> queue = getInitTasksQueue();
            queue.offer(new TableInitTask(mockTable));
            queue.offer(new TableInitTask(mockTable2));

            // Mock: tables don't exist
            when(metadataFactory.existsTable(any(SchemaAndTable.class), isNull())).thenReturn(false);

            CreateTableQuery createQuery2 = mock(CreateTableQuery.class);
            doReturn(createTableQuery).when(metadataFactory).createTable(mockTable);
            doReturn(createQuery2).when(metadataFactory).createTable(mockTable2);
            when(createTableQuery.ifExists()).thenReturn(createTableQuery);
            when(createQuery2.ifExists()).thenReturn(createQuery2);

            // Act
            InitProcessor processor = new InitProcessor(factory, scanOptions);
            processor.run();

            // Assert: both tasks were executed
            verify(metadataFactory, times(1)).createTable(mockTable);
            verify(metadataFactory, times(1)).createTable(mockTable2);
            verify(createTableQuery, times(1)).execute();
            verify(createQuery2, times(1)).execute();

            // Queue should be empty
            assertEquals(0, queue.size());
        }

        @Test
        @DisplayName("empty queue results in no task execution")
        void testEmptyQueueNoExecution() {
            // Arrange: no tasks queued

            // Act
            InitProcessor processor = new InitProcessor(factory, scanOptions);
            processor.run();

            // Assert: no metadata operations performed
            verify(metadataFactory, never()).existsTable(any(), any());
        }
    }

    @Nested
    @DisplayName("stopOnError behavior")
    class StopOnErrorBehavior {

        @Test
        @DisplayName("stopOnError=true: exception propagates, subsequent tasks not executed")
        void testStopOnErrorPropagatesException() throws Exception {
            // Arrange: queue two tasks
            RelationalPathEx<?> mockTable2 = mock(RelationalPathEx.class);
            SchemaAndTable sat1 = new SchemaAndTable(null, "fail_table");
            SchemaAndTable sat2 = new SchemaAndTable(null, "ok_table");

            when(mockTable.getSchemaAndTable()).thenReturn(sat1);
            when(mockTable.getInitializeData()).thenReturn(null);

            BlockingQueue<TableInitTask> queue = getInitTasksQueue();
            queue.offer(new TableInitTask(mockTable));
            queue.offer(new TableInitTask(mockTable2));

            // Mock: first table check throws exception
            when(metadataFactory.existsTable(sat1, null))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert: exception propagates (stopOnError defaults to true)
            InitProcessor processor = new InitProcessor(factory, scanOptions);
            RuntimeException thrown = assertThrows(RuntimeException.class, processor::run);
            assertEquals("Database connection failed", thrown.getMessage());

            // Assert: second table was never checked (subsequent task not executed)
            verify(metadataFactory, never()).existsTable(sat2, null);
            verify(metadataFactory, never()).createTable(mockTable2);
        }
    }
}
