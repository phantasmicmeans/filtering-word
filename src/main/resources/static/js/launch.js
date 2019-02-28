/**
 * filtering 된 결과 리턴 from redis
 */
function getFilteredResult(requestStr){
    var error = false;
    var result = null;

    $.ajax({
            url:'http://localhost:8081/v1/filter/result/search',
            datatype: 'json',
            type: 'get',
            async: false,
            data: JSON.stringify( {
                "requestStr" : requestStr
            }),
            success: function(data) {
                result = data;
            },
            error: function(err){
                error = true;
                console.log(err)
            }
    });

    return result;
}


/**
 * 금칙어를 받아 redis로 insert
 */
function postBWords(requestStr) {

    var error = false;를

    $.ajax({
        url: 'http://localhost:8081/v1/filter/redis/in',
        datatype: 'json',
        type: 'post',
        data: JSON.stringify({
            "requestStr" : requestStr
        }),
        success: function() {
            console.log("성공");
            alert("정상적으로 등록되었습니다");
        },
        error : function(err) {
            console.log(err);
            console.log("금칙어 저장 실패");
        }

    });
}

function deleteBWord(requestStr) {

    var result = false;

    $.ajax({
        url: 'http://localhost:8081/v1/filter/redis/out',
        datatype: 'json',
        type: 'post',
        data: JSON.stringify({
            "requestStr": requestStr
        }),
        success: function(){
            result = true;
        },
        error: function(err) {
            console.log(err);
            console.log("금칙어 저장 실패");
        }
    })

    return result;
}