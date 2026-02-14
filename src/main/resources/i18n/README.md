# 后端国际化（i18n）

## 字典翻译

接口返回的字典、字典项名称会通过 `DictUtils` 按当前语言翻译，key 规则：

- 字典名称：`dict.{dict.code}.name`
- 字典项名称：`dict.{dict.code}.item.{dictItem.code}`（`dictItem.code` 为数字时对应 YAML 中的 `"1"`、`"2"` 等字符串 key）

**每次在 SQL 中新增或变更字典（dict）/ 字典项（dict_item）时，必须在以下三个文件中追加对应翻译：**

- `messages_zh_CN.yml`
- `messages_ja.yml`
- `messages_en_US.yml`

在各自文件中的 `dict:` 下增加或修改与 `dict.code` 同名的节点，例如：

```yaml
dict:
  your_dict_code:
    name: "字典名称"
    item:
      "1": "选项一"
      "2": "选项二"
```

否则接口会回退为数据库中的原始 `name` 或 code 字符串。
