## 王者营地数据可视化

通过王者营地的数据 完成接口

### UserInfoController.java
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述 |
|---------|----------|----------|----------|----------|
| `/user/` | GET | `sid` (必填), `uid` (可选，默认为空) | ResponseEntity<String> | 获取用户信息并生成用户画像图片 |

### BattleController.java
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述 |
|---------|----------|----------|----------|----------|
| `/battle/history` | GET | `sid` (必填), [opt](file://org\json\JSONObject.java#L27-L27) (可选，默认为空), `uid` (可选，默认为空) | Object | 获取战斗历史记录并生成战斗历史图片 |

### BindController.java
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述 |
|---------|----------|----------|----------|----------|
| `/bind/` | GET | `sid` (必填), `uid` (必填) | ResponseEntity<String> | 绑定用户ID |
| `/bind/un` | GET | `sid` (必填), `uid` (必填) | ResponseEntity<String> | 解除绑定用户ID |
| `/bind/switch` | GET | `sid` (必填), `uid` (可选) | ResponseEntity<String> | 切换绑定用户ID |
| `/bind/get` | GET | `sid` (必填) | ResponseEntity<String> | 获取已绑定的用户ID列表 |
| `/bind/reload` | GET | 无参数 | ResponseEntity<String> | 重载绑定配置 |

以上是基于提供的代码生成的接口文档，包含了每个接口的路径、请求方法、参数说明、响应类型以及功能描述。