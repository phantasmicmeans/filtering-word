$('#bRequest').submit(function(e) {

    var requestStr = $("input[id='bword']").value();
    console.log("requestStr :" +requestStr);
    if(requestStr !== null) {
        getFilteredResult(requestStr);
    }

    else {
        console.log(e);
        alert("내용이 비어있습니다");
    }
});

$('#file-download-b').submit(function(e) {

    console.log("sdfsdfsdfsd");

        $.ajax({
            url:'http://localhost:8081/download/bword',
            datatype: 'json',
            type: 'get',
            async: false,
            success: function() {
                console.log("성공");
                alert("다운로드 완료");
            },
            error: function(err){
                console.log(err)
            }
        });
});

