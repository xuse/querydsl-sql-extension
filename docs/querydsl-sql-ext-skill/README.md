# ez-m-querydsl-sql-ext

[querydsl-sql-extension](https://github.com/xuse/querydsl-sql-extension) 框架的 AI 编程辅助 Skill，适用于 [Kiro IDE](https://kiro.dev)。

## 功能

安装后，AI 助手在编码时可获得本框架的 API 参考，包括：

- 框架初始化（Spring / 非 Spring）
- 实体定义与注解（`@TableSpec`、`@ColumnSpec`、`@CustomType` 等）
- CRUD 操作（GenericRepository、SQLQueryFactory、Lambda 查询）
- DDL 管理（建表、改表、分区）
- 表名路由（业务层分表）
- Record 类型映射

## 安装

**方式一：通过 EZSkillHub（如已安装）**

```powershell
ezskillhub install ez-m-querydsl-sql-ext
```

**方式二：手动安装**

将本目录复制到 Kiro 的 skills 目录：

```bash
# 全局安装（所有项目可用）
cp -r ez-m-querydsl-sql-ext ~/.kiro/skills/

# 项目级安装（仅当前项目可用）
cp -r ez-m-querydsl-sql-ext .kiro/skills/
```

## 使用

本 Skill 设置为手动触发（`inclusion: manual`）。在 Kiro 聊天中通过 `#ez-m-querydsl-sql-ext` 引用即可激活。

## 版本对应

| Skill 版本 | 框架版本 |
|-----------|---------|
| 1.0.0 | 5.0.0-r172 |
