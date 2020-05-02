<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<html>
<head>

    <link href="static/plusins/bootstrap/css/bootstrap.css" />
    <link href="static/plusins/bootstrapvalidator/css/bootstrapValidator.css"/>


    <style>
        .rightValidate {
            width: 280px;
            margin: 0px auto;
            position: relative;
            line-height: 33px;
            height: 33px;
            text-align: center;
            z-index: 99;
        }

        .v_rightBtn {
            position: absolute;
            left: 0;
            top: 0;
            height: 33px;
            width: 40px;
            background: #ddd;
            cursor: pointer;
        }
        .imgBtn{
            width:55px;
            height: 55px;
            position: absolute;
            left: 0;
            display: none;
        }
        .imgBtn img{
            width:100%
        }
        .imgBg{
            position: relative;
            width: 280px;
            height: 0;
            box-shadow: 0px 4px 8px #3C5476;
        }

        .hkinnerWrap{
            border: 1px solid #eee;
        }
        .green{
            border-color:#34C6C2 !important;
        }
        .green .v_rightBtn{
            background: #34C6C2;
            color: #fff;
        }
        .red{
            border-color:red !important;
        }
        .red .v_rightBtn{
            background: red;
            color: #fff;
        }
        .refresh{
            position: absolute;
            width: 30px;
            height: 30px;
            right: 0;
            top: 0;
            font-size: 12px;
            color: #fff;
            text-shadow: 0px 0px 9px #333;
            cursor: pointer;
            display: none;
        }
        .notSel{
            user-select: none;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            -webkit-touch-callout: none;
        }
    </style>


</head>
<body>
<div class="comImageValidate rightValidate">
    <h2>认证页</h2>

<form action="/auth/login" method="post">
        用户名：<input type="text" name="username"/><br/>
        密码：<input type="password" name="password"/><br/>
        <div class="hkinnerWrap" style="height: 30px;position: relative">
                <span  class="v_rightBtn "><em class="notSel">→</em></span>
                <span class="huakuai"  style="font-size: 12px;line-height: 30px;color: #A9A9A9;">向右滑动滑块填充拼图</span>
                <input type = "hidden" name="validX"/>
            </div>
            <div class="imgBg">
                <div class="imgBtn">
                    <img />
                </div>
                <span class="refresh">刷新</span>
            </div>
        <input type="submit"/>
    </form>
</div>

<br>


${sessionScope.SPRING_SECURITY_LAST_EXCEPTION.message}

</body>

<script type="text/javascript" src="static/js/jquery/jquery-3.4.1.min.js"></script>
<script type="text/javascript" src="static/js/layer.js"></script>
<script src="static/plusins/bootstrap/js/bootstrap.js"></script>
<script src="static/plusins/bootstrapvalidator/js/bootstrapValidator.js"></script>


<script>
    var tokenId = "";
    var y = "";
    var x = "";
    $(".comImageValidate").ready(function () {
        validateImageInit();
        $(".refresh").click(function () {
            validateImageInit();
        })
        $(".hkinnerWrap").mouseover(function(){
            $(".imgBg").stop(false).animate({"height":"171px"},100,function () {
                $(".imgBtn").css("display","block");
                $(".refresh").css("display","block");
            });
        }).mouseleave(function () {
            $(".imgBg").stop(false).animate({"height":"0"},100,function () {
                $(".imgBtn").css("display","none");
                $(".refresh").css("display","none");
            });
        });
        $(".imgBg").mouseover(function () {
            $(".imgBg").stop(false).animate({"height":"171px"},100,function () {
                $(".imgBtn").css("display","block");
                $(".refresh").css("display","block");
            });
        }).mouseleave(function () {
            $(".imgBg").stop(false).animate({"height":"0"},100,function () {
                $(".imgBtn").css("display","none");
                $(".refresh").css("display","none");
            });
        })
        $('.v_rightBtn').on({
            mousedown: function(e) {
                $(".huakuai").html("");
                $(".hkinnerWrap").removeClass("red green")
                var el = $(this);
                var os = el.offset();
                dx = e.pageX - os.left;
                //$(document)
                $(this).parents(".hkinnerWrap").off('mousemove');
                $(this).parents(".hkinnerWrap").on('mousemove', function(e) {
                    var newLeft=e.pageX - dx;
                    el.offset({
                        left: newLeft
                    });
                    var newL=parseInt($(".v_rightBtn").css("left"));
                    if(newL<=0){
                        newL=0;
                    }else if (newL>=228){
                        newL=236;
                    }
                    $(".v_rightBtn").css("left",newL+"px");
                    $(".imgBtn").offset({
                        left: newLeft
                    });
                    $(".imgBtn").css("left",newL+"px")
                }).on('mouseup', function(e) {
                    //$(document)
                    $(this).off('mousemove');
                })
            }
        }).on("mouseup",function () {
            $(this).parents(".hkinnerWrap").off('mousemove');
            var l=$(this).css("left");
            if(l.indexOf("px")!=-1){
                l=l.substring(0,l.length-2);
            }
            x = l;
            submitDate(l,y,tokenId)
        })

    });
    /*图形验证*/
    function submitDate(x,y,tokenId) {
        $.ajax({
            url:"${base}/validator/check?X="+x+"&Y="+y+"&token="+tokenId,
            dataType:'json',
            type: "POST",
            success:function (data) {
                if(data==true){
                    $(".hkinnerWrap").addClass("green").removeClass("red");
                    $(".hkinnerWrap input[name='validX']").val(x);
                } else {
                    $(".hkinnerWrap").addClass("red").removeClass("green");
                     setTimeout(function(){
                         $(".hkinnerWrap").removeClass("red green");
                         $(".v_rightBtn").css("left",0);
                         $(".imgBtn").css("left",0);
                     },500)
                    validateImageInit();
                }
            }
        })
    }

    /*初始化图形验证码*/
    function validateImageInit() {
        $.ajax({
            url:"/validator/init",
            dataType:'json',
            cache:false,
            type: "get",
            success:function (data) {
                $(".huakuai").html("向右滑动滑块填充拼图");
                $(".imgBg").css("background",'#fff url("data:image/jpg;base64,'+data.sourceImage+'")');
                $(".imgBtn").css('top',data.Y+'px');
                $(".imgBtn").find("img").attr("src","data:image/png;base64,"+data.newImage)
                tokenId=data.token;
                y=data.Y;
                $(".hkinnerWrap").removeClass("red green");
                $(".v_rightBtn").css("left",0);
                $(".imgBtn").css("left",0);
            },error:function(err){
                console.log(err)
            }
        })
    }
    /**
     * 再次进行用户拖动行为验证
     * @returns {boolean}
     */
    function reValidateDeal(){
        var result = false;
        $.ajax({
            url:"/validator/check?X="+x+"&Y="+y+"&token="+tokenId,
            dataType:'json',
            cache : false,
            async : false,
            type: "POST",
            success:function (data) {
                if(data==true){
                    result = true;
                }else {
                    result = false;
                }
            }
        })
        return result;
    }

</script>
</html>
