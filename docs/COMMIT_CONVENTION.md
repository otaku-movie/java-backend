# Git 提交信息规范

**Otaku Movie 各子仓库统一采用本规范。**

本仓库（java-backend）使用 [Conventional Commits](https://www.conventionalcommits.org/)，并通过 **Husky + commitlint** 在 `git commit` 时自动校验。

## 格式

```text
<type>(<scope>): <subject>

<空行>

- 修改内容 1
- 修改内容 2
- 修改内容 3
```

| 部分 | 说明 |
|------|------|
| `type` | 变更类型，见下表 |
| `scope` | 影响范围（可选），如 `auth`、`movie`、`flyway` |
| `subject` | 一句话概括「做了什么」，中文即可，≤100 字，句末不加句号 |
| 空行 | **主题与正文之间必须空一行** |
| 正文 | **必填**，每条修改以 `- ` 开头的列表项书写，写清具体改动而非重复标题 |

## type 一览

| type | 用途 |
|------|------|
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `docs` | 仅文档 |
| `style` | 样式/格式（不改变逻辑） |
| `refactor` | 重构（非新功能、非修 bug） |
| `perf` | 性能优化 |
| `test` | 测试 |
| `build` | 构建/依赖 |
| `ci` | CI 配置 |
| `chore` | 杂项（工具、配置等） |
| `revert` | 回滚 |

## 示例

```text
feat(auth): 登录写入 last_login_source 字段

- User 表新增 last_login_source 列（Flyway V81）
- H5/App 登录接口按平台写入 WEB / IOS / ANDROID
- 补充单元测试覆盖空值与枚举
```

## 提交命令示例

```bash
git commit -m "$(cat <<'EOF'
fix(movie): 修复场次列表 saleStatus 过滤条件

- 修正 MyBatis 查询 open 与 sale_status 组合逻辑
- 补充集成测试用例
EOF
)"
```

## 常见驳回原因

- 主题与正文之间**没有空行** → 加一行空行
- **没有正文**或正文不是 `- ` 列表 → 按上表补充修改点
- `type` 不在允许列表中 → 改用上表中的 type
- 主题超过 100 字 → 缩短 subject，细节放正文

## 相关配置

- 规则实现：[`commitlint.config.cjs`](../commitlint.config.cjs)
- Git 钩子：[`.husky/commit-msg`](../.husky/commit-msg)
- 首次克隆后执行 `npm install` 以安装 Husky 钩子
