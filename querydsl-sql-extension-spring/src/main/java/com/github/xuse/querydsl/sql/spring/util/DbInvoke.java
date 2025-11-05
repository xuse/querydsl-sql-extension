package com.github.xuse.querydsl.sql.spring.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.function.QueryFunction;
import com.mysema.commons.lang.CloseableIterator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class DbInvoke {
    private final DataSource ds;

    public static DbInvoke create(DataSource ds) {
        return new DbInvoke(ds);
    }

    DbInvoke(DataSource ds) {
        this.ds = ds;
    }

    public int execute(String sql, Object... params) {
        try (Connection conn = ds.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                setParams(st, Arrays.asList(params));
                return st.executeUpdate();
            }
        } catch (SQLException e) {
            throw Exceptions.toRuntime(e);
        }
    }

    public Fetcher fetch(String sql, Object... params) {
        return new Fetcher(sql, Arrays.asList(params));
    }

    public class Fetcher {
        private String sql;
        private List<Object> params;
        private int max;
        private int timeout;
        private int fetchSize;

        Fetcher(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }

        public Fetcher timeout(int seconds) {
            this.timeout = seconds;
            return this;
        }

        public Fetcher maxRows(int rows) {
            this.max = rows;
            return this;
        }

        public Fetcher fetchSize(int fetchSize) {
            this.fetchSize = fetchSize;
            return this;
        }

        public <T> Flux<T> iterator(QueryFunction<ResultSet, T> func) {
            Connection conn = null;
            PreparedStatement st = null;
            ResultSet rs = null;
            try {
                conn = ds.getConnection();
                st = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT);
                setParams(st, params);
                if (max > 0) {
                    st.setMaxRows(max);
                }
                if (timeout > 0) {
                    st.setQueryTimeout(timeout);
                }
                if (fetchSize > 0) {
                    st.setFetchSize(fetchSize);
                }
                rs = st.executeQuery();
            }catch(Exception e) {
                release(rs,st,conn);
                throw Exceptions.toRuntime(e);
            }
            ResultSetIteraor<T> iter = new ResultSetIteraor<T>(rs, st, conn, func);
            return Flux.fromIterable(Streams.wrap(iter)).doAfterTerminate(iter::close);
        }

        public <T> List<T> list(QueryFunction<ResultSet, T> func) {
            return fetch0((result) -> {
                try {
                    List<T> list = new ArrayList<>();
                    while (result.next()) {
                        list.add(func.apply(result));
                    }
                    return list;
                } catch (SQLException e) {
                    throw Exceptions.toRuntime(e);
                }
            });
        }

        public <T> Optional<T> first(QueryFunction<ResultSet, T> func) {
            this.max = 1;
            return fetch0((result) -> {
                try {
                    return result.next() ? Optional.of(func.apply(result)) : Optional.empty();
                } catch (SQLException e) {
                    throw Exceptions.toRuntime(e);
                }
            });
        }

        private <T> T fetch0(QueryFunction<ResultSet, T> func) {
            try (Connection conn = ds.getConnection()) {
                try (PreparedStatement st = conn.prepareStatement(sql)) {
                    setParams(st, Arrays.asList(params));
                    if (max > 0) {
                        st.setMaxRows(max);
                    }
                    if (timeout > 0) {
                        st.setQueryTimeout(timeout);
                    }
                    if (fetchSize > 0) {
                        st.setFetchSize(fetchSize);
                    }
                    try (ResultSet result = st.executeQuery()) {
                        return func.apply(result);
                    }
                }
            } catch (SQLException e) {
                throw Exceptions.toRuntime(e);
            }
        }
    }

    private void setParams(PreparedStatement st, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            param = adjust(param);
            st.setObject(i + 1, param);
        }
    }

    private Object adjust(Object param) {
        if (param == null) {
            return null;
        }
        if (param.getClass() == java.util.Date.class) {
            return new Timestamp(((Date) param).getTime());
        }
        return param;
    }

    static final class ResultSetIteraor<T> implements CloseableIterator<T> {
        private ResultSet rs;
        private Statement st;
        private Connection conn;
        private QueryFunction<ResultSet, T> func;
        private boolean notTaken = false;

        @Override
        @SneakyThrows
        public boolean hasNext() {
            return notTaken || (notTaken = rs.next());
        }

        @SneakyThrows
        @Override
        public T next() {
            if(!notTaken) {
                rs.next();
            }
            notTaken=false;
            return func.apply(rs);
        }

        @Override
        public void close() {
            release(rs, st, conn);
        }

        ResultSetIteraor(ResultSet rs, PreparedStatement st, Connection conn, QueryFunction<ResultSet, T> func) {
            this.rs = rs;
            this.st = st;
            this.conn = conn;
            this.func = func;
           
        }
    }

    static final void release(ResultSet rs, Statement st, Connection conn) {
        int exceptions = 0;
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                exceptions++;
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (Exception e) {
                exceptions++;
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                exceptions++;
            }
        }
        log.info("Connection released. [{}]@ [{}]", rs, conn);
        if (exceptions > 0) {
            log.warn("Caught exceptions {} while closing database context.", exceptions);
        }
    }
}
