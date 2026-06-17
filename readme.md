## 王者营地数据可视化

> ~~⚠️ **公告 2026/03/25**：官方数据加密，当前项目暂未处理~~

DEMO-PRE (项目预览) : http://wv.kloping.top/

通过王者营地的数据 完成接口

参考/借鉴 项目 <https://github.com/KimigaiiWuyi/WzryUID>

### UidQueryController
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述        |
|---------|----------|----------|----------|-------------|
| `/uid/query` | GET | `sid` (必填), `uid` (必填) |string| 获取用户已绑定ID信息 |

### UserInfoController
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述 |
|---------|----------|----------|----------|----------|
| `/user/` | GET | `sid` (必填), `uid` (可选，默认为空) |string| 获取用户信息并生成用户画像图片 |

### BattleController
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述 |
|---------|----------|----------|----------|----------|
| `/battle/history` | GET | `sid` (必填), [opt](file://org\json\JSONObject#L27-L27) (可选，默认为空), `uid` (可选，默认为空) | Object | 获取战斗历史记录并生成战斗历史图片 |
| `/battle/preview` | GET | `sid` (可选), [opt](file://org\json\JSONObject#L27-L27) (可选，默认为空), `uid` (可选，默认为空) | string | 预览战斗历史记录的文字统计信息 |

> opt 可以是 排位,巅峰,标注,娱乐 或 _英雄名_;
> 
>参考: https://pvp.qq.com/web201605/js/herolist.json 实时数据 

### BindController
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述 |
|---------|----------|----------|----------|----------|
| `/bind/` | GET | `sid` (必填), `uid` (必填) |string| 绑定用户ID |
| `/bind/un` | GET | `sid` (必填), `uid` (必填) |string| 解除绑定用户ID |
| `/bind/switch` | GET | `sid` (必填), `uid` (可选) |string| 切换绑定用户ID |
| `/bind/get` | GET | `sid` (必填) |string| 获取已绑定的用户ID列表 |
| `/bind/reload` | GET | 无参数 |string| 重载绑定配置 |

### IntegratedQueryController
| 接口路径 | 请求方法 | 参数说明 | 响应类型 | 功能描述 |
|---------|----------|----------|----------|----------|
| `/integrated/query` | GET | `name` (可选), `uid` (可选), `opt` (可选，默认全部) | JSON | 集成查询用户+用户信息+战斗信息 |

**使用说明：**

1. **通过昵称查询**: `/integrated/query?name=xxx`
   - 如果查到 **0** 个用户 → 返回错误提示
   - 如果查到 **1** 个用户 → 自动返回该用户的完整信息（用户信息 + 战斗历史统计）
   - 如果查到 **多个** 用户 → 返回用户列表，需要选择（`needSelect: true`）

2. **通过UID直接查询**: `/integrated/query?uid=xxx`
   - 直接返回指定用户的完整信息

3. **战斗类型筛选**: `/integrated/query?uid=xxx&opt=排位`
   - `opt` 可选值：`排位` / `巅峰` / `标准` / `娱乐`
   - 默认不筛选，返回全部战斗历史

**响应示例（多用户需要选择）：**
```json
{
  "needSelect": true,
  "message": "查询到多个用户，请选择其中一个进行查询",
  "userCount": 3,
  "users": [
    { "uid": "123456", "name": "Player1", "region": "微信XX区", "level": "30", "avatar": "...", "dw": "最强王者" },
    ...
  ]
}
```

**响应示例（单个用户完整信息）：**
```json
{
  "uid": "123456",
  "userInfo": { "roleId": "...", "roleName": "...", "roleDesc": "...", ... },
  "profile": { ... },
  "profileIndex": { ... },
  "heroList": { ... },
  "battleStats": {
    "totalCount": 12,
    "winCount": 8,
    "mvpCount": 2,
    "winRate": "66.7%",
    "typeStats": [
      { "type": "排位赛", "count": 5, "wins": 3, "winRate": "60.0%" },
      ...
    ]
  },
  "battleList": [ ... ]
}
```

---

## 免责声明
本仓库所有代码、文档仅作个人编程学习、技术研究用途，仅针对公开网页/客户端展示的公开数据做技术演示，无任何商业用途。
1. 王者荣耀相关游戏素材、文字、图标、商标、游戏数据知识产权均归属腾讯游戏及其关联公司所有；
2. 使用者自愿使用本项目，因私自运行、分发、二次改造本项目产生的全部风险、法律责任由使用者本人独立承担，项目作者不承担任何连带责任；
3. 若权利人认为本仓库内容存在侵权，请通过预留联系方式告知，收到通知后会第一时间下架、删除全部相关内容。
   联系微信：kloping_
