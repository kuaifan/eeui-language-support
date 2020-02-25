## 文件说明

```text
modules.json ---- 模块
components.json ---- 组件
variables.json ---- 变量
```


## 获取modules.json内容

```js
var con = "";
$("h2").each(function(index, item) {
    $(item).find(".md-badge-value").remove();
	var name = $(item).text().trim();
	name = name.substring(name.indexOf("eeui.") + 5);
	var desc = $(item).next().text();
	con+= '{"name": "' + name + '", "desc": "' + desc + '"},\n';
});
function copyText(text) {
    var textarea = document.createElement("textarea");//创建input对象
    var currentFocus = document.activeElement;//当前获得焦点的元素
    document.body.appendChild(textarea);//添加元素
    textarea.value = text;
    textarea.focus();
    if(textarea.setSelectionRange)
        textarea.setSelectionRange(0, textarea.value.length);//获取光标起始位置到结束位置
    else
        textarea.select();
    try {
        var flag = document.execCommand("copy");//执行复制
    } catch(eo) {
        var flag = false;
    }
    document.body.removeChild(textarea);//删除元素
    currentFocus.focus();
    return flag;
}
copyText(con);
```


## 获取components.json内容

```js
var data = {};

data.name = $("h1").text().substring(1).trim(),
data.desc = $("h1").next().text().replace(/<(.*?)>\s*是*/, '');
data.url = window.location.href;

data.attrs = [];

$("tbody>tr").each(function(index, item){
	var td = $(item).find("td");
	if (td.length == 4) {
		var option = [];
		if ($(td[1]).text().indexOf("Boolean") != -1) {
			option.push({"value": "true", "desc": ""});
			option.push({"value": "false", "desc": ""});
		}
		data.attrs.push({
			name: $(td[0]).text(),
			desc: $(td[2]).text(),
			type: $(td[1]).text(),
			defval: $(td[3]).text(),
			option: option,
		});
	}
});
function copyText(text) {
    var textarea = document.createElement("textarea");//创建input对象
    var currentFocus = document.activeElement;//当前获得焦点的元素
    document.body.appendChild(textarea);//添加元素
    textarea.value = text;
    textarea.focus();
    if(textarea.setSelectionRange)
        textarea.setSelectionRange(0, textarea.value.length);//获取光标起始位置到结束位置
    else
        textarea.select();
    try {
        var flag = document.execCommand("copy");//执行复制
    } catch(eo) {
        var flag = false;
    }
    document.body.removeChild(textarea);//删除元素
    currentFocus.focus();
    return flag;
}
copyText(JSON.stringify(data, null, "\t"));


window.location.href = $("ul.sidebar-links").find("a.active").parent().next("li").find("a").attr("href");
```