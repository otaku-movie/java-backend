/**
 * 提交信息规范（Conventional Commits + 正文列表）。
 * 全仓统一格式，详见 docs/COMMIT_CONVENTION.md
 */

/** Git commit message trailer，不计入正文列表校验。 */
const TRAILER_LINE = /^[A-Za-z-]+:\s/

/** @param {import('@commitlint/types').Commit} parsed */
function bodyBulletsRule(parsed) {
  const body = parsed.body ?? ''
  const lines = body
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  const contentLines = lines.filter((line) => !TRAILER_LINE.test(line))

  if (!contentLines.length) {
    return [
      false,
      '提交正文不能为空：主题与正文之间空一行，用「- 」列出具体修改（见 docs/COMMIT_CONVENTION.md）'
    ]
  }

  const invalid = contentLines.filter((line) => !/^-\s.+/.test(line))
  if (invalid.length) {
    return [false, `正文每行须以「- 」开头，不合规：${invalid[0]}`]
  }

  return [true]
}

module.exports = {
  extends: ['@commitlint/config-conventional'],
  plugins: [
    {
      rules: {
        'body-bullets': bodyBulletsRule
      }
    }
  ],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'docs', 'style', 'refactor', 'perf', 'test', 'build', 'ci', 'chore', 'revert']
    ],
    'subject-case': [0],
    'subject-max-length': [2, 'always', 100],
    'body-leading-blank': [2, 'always'],
    'body-bullets': [2, 'always']
  }
}
