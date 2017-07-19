# cordova-plugin-lz-vspeaker
科大讯飞声纹验证cordova插件

## Example

```javascript
lzVSpeaker.register(
      function (msg) {
        console.log("====讯飞SDK返回数据==" + msg);
      },"595311f0","test6666");

lzVSpeaker.verify(
      function (msg) {
        console.log("====讯飞SDK返回数据==" + msg);
      },"595311f0","test6666");

```

## 接口分为“注册”和“验证”连个接口

用户需要先注册才能使用验证接口。

+ 注册：    用户读一串文字，讯飞平台会分析用户的声音，获取一些声音特征。完成注册。
+ 验证：    用户读一串文字，讯飞根据用户注册时的的声纹特征判断是不是用户本人。

**插件实时返回讯飞sdk的数据.**
## 数据返回格式  

```json
{
    "code": 1200,         //返回码---数字类型
    "msg": "当前正在说话，音量大小：8",    //提示信息----字符串
    "pwd": "密码",        //需要用户读的文字,仅在----字符串
    "volume": 8            //实时返回用户声音的大小--数字
}
```
---
## 接口数据说明

* 1200 返回音频大小.音频随用户发声,实时变化.
```json
{
    "code":1200 //返回音量
    "volume":8  //音量
    "msg":"当前正在说话，音量大小：8"
}
```
* 1300 返回文字--用于声纹训练,声纹验证
```json
{
    "code":"1300"
    "msg":"请读出：密码"
    "pwd":"密码"
}
```
* 1400 开始说话
```json
{
    "code":"1400"
    "msg":"开始说话"
}
```
* 1401 结束说话
```json
{
    "code":"1401"
    "msg":"结束说话"
}
```
* 1500 注册/验证 失败
详细原因见msg信息:获取密码失败,重复注册,未注册,达到验证最大次数等等都会导致失败
```json
{
    "code":"1500"
    "msg":"讯飞sdk返回的错误描述"
}
```
* 10000-12000 讯飞无法识别用户的语音,msg返回无法识别原因
```json
{
    "code":"11603"//示例
    "msg":"太多噪音"
}
```

### 注册接口专有返回的数据
+ 1100 注册成功
```json
{
    "code":"1100"
    "msg":"注册成功"
}
```
+ 1101 注册失败 --暂时未使用
```json
{
    "code":"1101"
    "msg":"注册失败，请重新开始。"
}
```

### 验证接口专有返回数据
- 1110 未注册,请先注册
```json
{
    "code": 1110,
    "msg": "模型不存在，请先注册"
}
```
- 1102 验证通过
```json
{
    "code":"1102"
    "msg":"验证通过！"
}
```
- 1103 验证不通过
```json
{
    "code":"1103"
    "msg":"验证不通过！"
}
```