# TcsExportAllPlayersId

Minecraft 插件，导出服务器所有离线玩家 ID（名称），支持敏感词过滤与重复去除，并分别输出到两个 CSV 文件。

## 命令

| 命令 | 说明 |
|---|---|
| `/tcsexportallplayersid export` | 导出所有玩家 ID（过滤敏感词、去重） |
| `/tcsexportallplayersid reload` | 重新加载敏感词配置 |

## 权限

| 权限节点 | 说明 |
|---|---|
| `tcsexportallplayersid.export` | 允许执行 `export` 导出 |
| `tcsexportallplayersid.reload` | 允许执行 `reload` 重载配置 |

无权限时提示：`你没有权限执行此命令！`

## 配置

首次启动会在 `plugins/TcsExportAllPlayersId/config.yml` 生成配置文件。敏感词支持正则表达式，匹配不区分大小写。

```yaml
sensitive-words:
  - "敏感词1"
  - "badword"
  - "正则表达式示例\\d+"