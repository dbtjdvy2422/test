<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<h1>Upload with Ajax</h1>

<div class="bigPictureWrapper">
    <div class="bigPicture">

    </div>
</div>

<style>
    .uploadResult {
        width: 100%;
        background-color: gray;
    }

    .uploadResult ul {
        display: flex;
        flex-flow: row;
        justify-content: center;
        align-items: center;
    }

    .uploadResult ul li {
        list-style: none;
        padding: 10px;
        align-content: center;
        text-align: center;
    }

    .uploadResult ul li img {
        width: 100px;
    }

    .uploadResult ul li span {
        color: white;
    }

    .bigPictureWrapper {
        position: absolute;
        display: none;
        justify-content: center;
        align-items: center;
        top: 0%;
        width: 100%;
        height: 100%;
        background-color: gray;
        z-index: 100;
        background: rgba(255,255,255,0.5);
    }

    .bigPicture {
        position: relative;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .bigPicture img {
        width: 600px;
    }

</style>

<body>
<h1>Upload with Ajax</h1>
<div class="uploadDiv">
    <input type="file" name="uploadFile" multiple>
</div>

<div class="uploadResult">
    <ul>

    </ul>
</div>

<button id="uploadBtn">Upload</button>
</body>

<script type="text/javascript">
    function showImage(fileCallPath) {
        // alert("fileCallPath: " + fileCallPath);

        $(".bigPictureWrapper").css("display", "flex").show();

        $(".bigPicture").html("<img src='/display?fileName=" + encodeURI(fileCallPath) + "'>")
            .animate({width: "100%", height: "100%"}, 1000);

    }
    $(document).ready(function() {
        var regex = new RegExp("(.*?)\.(exe|sh|zip|alz)$");
        var maxSize = 5242880; // 5MB

        function checkExtenson(fileName, fileSize) {
            if (fileSize >= maxSize) {
                alert("?????? ????????? ??????");
                return false;
            }

            if (regex.test(fileName)) {
                alert("?????? ????????? ????????? ???????????? ??? ????????????.");
                return false;
            }
            return true;
        }

        var cloneObj = $(".uploadDiv").clone();

        $("#uploadBtn").on("click", function(e) {
            // jQuery??? ???????????? ????????? ?????? ???????????? FormData?????? ????????? ???????????? ??????. (??????????????? ????????? ???????????? ??????) - ?????? ?????? ????????? form ????????? ????????? ???????????? ??????.
            var formData = new FormData();
            var inputFile = $("input[name='uploadFile']");
            var files = inputFile[0].files;

            /* alert("formData: " + formData);
            console.log("formData: ", formData); */


            console.log(files);

            // add filedate to formdata
            for (var i = 0; i < files.length; i++) {
                if (!checkExtenson(files[i].name, files[i].size)) {
                    return false;
                }

                formData.append("uploadFile", files[i]);
            }

            $.ajax({
                url: "/uploadAjaxAction",
                processData: false,
                contentType: false,
                data: formData,
                type: "POST",
                dataType: "json",
                success: function(result) {
                    alert("Uploaded: " + result);
                    console.log("result: ", result);

                    // ?????? ?????? ?????? - ?????? ??????, ????????? ???
                    showUploadedFile(result);

                    // ?????? ?????????
                    $(".uploadDiv").html(cloneObj.html());
                }
            });

        });

        var uploadResult = $(".uploadResult ul");

        function showUploadedFile(uploadResultArr) {
            var str = "";

            $(uploadResultArr).each(function(i, obj) {
                if (!obj.image) {
                    // str += "<li><img src='/resources/img/test-image.jpg'>" + obj.fileName + "</li>";
                    var fileCallPath = encodeURIComponent(obj.uploadPath + "/" + obj.uuid + "_" + obj.fileName);

                    var fileLink = fileCallPath.replace(new RegExp(/\\/g), "/");

                    str += "<li><div><a href='/download?fileName=" + fileCallPath + "'>"
                        + "<img src='/resources/img/test-image.jpg'>" + obj.fileName
                        + "</a><span data-file=\'" + fileCallPath + "\' data-type='file'> X </span></div></li>";

                } else {
                    // encodeURIComponent: GET ?????? ?????? ????????? ????????? ????????? ?????? ????????? ?????? ???????????? ????????? ??? ??? ??????.
                    // -> ?????? ???????????? ?????? URI ????????? ????????? ???????????? ????????? ??????????????????. (????????? IE??? ?????? ?????? ????????? ???????????? ??????????????? ????????? ?????? ??? ??????.)
                    /* str += "<li>" + obj.fileName + "</li>"; */
                    var fileCallPath = encodeURIComponent(obj.uploadPath + "/s_" + obj.uuid + "_" + obj.fileName);
                    var originPath = obj.uploadPath + "\\" + obj.uuid + "_" + obj.fileName;
                    originPath = originPath.replace(new RegExp(/\\/g), "/");

                    str += "<li><a href=\"javascript:showImage(\'" + originPath + "\')\"><img src='/display?fileName=" + fileCallPath
                        + "'></a><span data-file=\'" + fileCallPath + "\' data-type='image'> X </span></li>";

                }
            });

            uploadResult.append(str);
        }

        $(".bigPictureWrapper").on("click", function(e) {
            $(".bigPicture").animate({width: "0%", height: "0%"}, 1000);
            // ES6??? ????????? ?????? (=>)??? ??????????????? ?????? ???????????????, IE 11????????? ????????? ???????????? ?????????.
            /* setTimeout(() => {
                $(this).hide();
            }, 1000);  */

            setTimeout(function() {
                $(".bigPictureWrapper").hide();
            }, 1000);

        });

        $(".uploadResult").on("click", "span", function(e) {
            var targetFile = $(this).data("file");
            var type = $(this).data("type");
            console.log(targetFile);

            $.ajax({
                url: "/deleteFile",
                data: {
                    fileName: targetFile,
                    type: type
                },
                dataType: "text",
                type: "POST",
                success: function(result) {
                    alert("result: " + result);
                }
            });
        });

    });
</script>


</html>
