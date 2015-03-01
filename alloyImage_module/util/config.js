/**
 * @description 常用配置
 */

exports.config = {
    protocle : "http://",
//    testDomain : "testeyk",
//    moniDomain : "monieyk",
//    prodDomain : "eyuanku",
    commonSuffix : "image.oss-cn-beijing-internal.aliyuncs.com/materials",
    defaultUrl :   "http://eyuankupub.oss-cn-beijing-internal.aliyuncs.com/sys/pic.png",
    getBaseUrl:function(ossEnv){
        return this.protocle + ossEnv + this.commonSuffix;
    },
    getImgUrl : function(pathname, params,baseUrl){
        var imgUrl = (params.Expires && params.OSSAccessKeyId && params.Signature)
                ? baseUrl + pathname
                + "?Expires=" + params.Expires
                + "&OSSAccessKeyId=" + params.OSSAccessKeyId
                + "&Signature=" + encodeURIComponent(params.Signature)
                : this.defaultUrl;
        return imgUrl;
    }
}
