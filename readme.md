## 目地
### 1.学习拉动款图形验证
### 2.加深SpringSecurity


## 拉动示图形验证原理

### 1.图形验证的生成
首先准备3张图片，一张为原图，第二张是抠图板，最后一张为抠图描边图片。
准备一个请要求Action的处理代理，返回值为抠掉的图、抠出图片、还有抠出图的X,Y坐标。并将扣出图的X,Y坐标保存到
session中以供后期的比较。
### 2.前端请求
在打开认页时以AJAX的方式请求第二步的Action，将返回的流式图片加入img标签，变调理
抠出图片的css样式（主要是其位置,就是X,Y坐标）
### 3.后端验证
前端拉动抠出图片，在鼠标弹起事件中，对抠出图片的X,Y坐标以AJAX方式发送给后端验证Action。如X,Y坐标值和
Session中的值一致，就返回OK，否则NG。前端接到返回值，则启用登陆按钮，否则重新请求生成新的验证码。

## 小结
手动输入验证码、拉动式验码、点字式验证、短信验证等等，它们原理基本都是后端生成验证值并以一定有效时间保存到session、redis中，前端发送验证值给后端，
后端取出后再和以保存的值进行比较，根据比较结果返回相应的值，前端收到后根据项目具体的业务规则做出对应的处理。

<a href="emailto:hewlh@163.com">hewlh@163.com</a>


