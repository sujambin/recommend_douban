

$(function(){
    if($("#userId").val()==""){
        $("#userId").val("3")
        formatRecommendBooks();
    }
    $('#getRecdBooksByUserId').click(formatRecommendBooks);
});



/*加载用户已经看过的书籍*/
function getReadedBooksByUserId() {
    var userId = $("#userId").val();

    $.get("/getReadedBooksByUserId", {
        userId: userId,
    }, function(data) {
        /*到时候多种书籍的时候，加载书籍的函数要分离*/
        var length = data.readedBook.length, i;
        for(i= 0; i< length; i++) {
            var aim = ".readedBook:eq("+ i+ ") ";

            $(aim + "dt a").attr('href', 'https://book.douban.com/subject/'+data.readedBook[i].id);
            $(aim + "dt a img").attr('src', data.readedBook[i].image);
            $(aim + "dd a").attr('href', 'https://book.douban.com/subject/'+data.readedBook[i].id);
            $(aim + "dd a").html(data.readedBook[i].title);
            $(aim + "dd button").html(data.readedBook[i].rating+'分');
        }
        if(i == 0) {
            var aim = ".recdBook:eq("+ i+ ") ";

        }
    }, 'json');
}

/*加载推荐爱的书本书本*/
function getRecdBooksByUserId() {
    // getReadedBooksByUserId();
    formatReadedBooks();
    var userId = $("#userId").val();

    $.get("/getRecdBooksByUserId", {
        userId: userId,
    }, function(data) {
        /*到时候多种书籍的时候，加载书籍的函数要分离*/
        var length = data.recdBooks.length, i;
        for(i= 0; i< length; i++) {
            var aim = ".recdBook:eq("+ i+ ") ";

            $(aim + "dt a").attr('href', 'https://book.douban.com/subject/'+data.recdBooks[i].id);
            $(aim + "dt a img").attr('src', data.recdBooks[i].image);
            $(aim + "dd a").attr('href', 'https://book.douban.com/subject/'+data.recdBooks[i].id);
            $(aim + "dd a").html(data.recdBooks[i].title);
            $(aim + "dd button").html(data.recdBooks[i].rating+'分');
        }
        if(i == 0) {
            var aim = ".recdBook:eq("+ i+ ") ";

            $(aim + "dt a").attr('href', 'https://book.douban.com/subject/'+data.recdBooks[i].id);
            $(aim + "dt a img").attr('src', data.recdBooks[i].image);
            $(aim + "dd a").attr('href', 'https://book.douban.com/subject/'+data.recdBooks[i].id);
            $(aim + "dd a").html(data.recdBooks[i].title);
            $(aim + "dd button").html(data.recdBooks[i].rating);

        }
    }, 'json');
}

/*加载用户已经看过的书籍*/
function formatReadedBooks() {
    var userId = $("#userId").val();
    $.get("/getReadedBooksByUserId", {
        userId: userId,
    }, function(data) {
        $(".userReadedBook").html("");
        var html = ""
        var length = data.readedBook.length, i;
        for(i= 0; i< length; i++) {
            html = html+
                "<dl class=\"readedBook\">\n" +
                "            <dt>\n" +
                "                <a href=\"https://book.douban.com/subject/"+data.readedBook[i].id+"\" onclick=\"moreurl(this, {'total': 10, 'clicked': '30143057', 'pos': 0, 'identifier': 'book-rec-books'})\">" +
                "                <img class=\"m_sub_img\" src=\""+data.readedBook[i].image+"\"></a>\n" +
                "            </dt>\n" +
                "            <dd>\n" +
                "                <a href=\"https://book.douban.com/subject/"+data.readedBook[i].id+" \" onclick=\"moreurl(this, {'total': 10, 'clicked': '30143057', 'pos': 0, 'identifier': 'book-rec-books'})\" class=\"\">\n"
                +                   data.readedBook[i].title+
                "                </a>\n" +
                "                <button type=\"button\" class=\"btn-xs btn-info\">"+data.readedBook[i].rating+"分</button>\n" +
                "            </dd>\n" +
                "</dl>"
            if ((i+1)%5==0){
                html = html+"<dl class=\"clear\"></dl>";
            }
        }
        if(i == 0) {
            html="<h3>该用户没有看过的书籍</h3>"
        }
        $(".userReadedBook").html(html);
    }, 'json');
}


/*加载用户已经看过的书籍*/
function formatRecommendBooks() {
    formatReadedBooks();
    var userId = $("#userId").val();
    $.get("/getRecdBooksByUserId", {
        userId: userId,
    }, function(data) {
        $(".recommentBooks").html("");
        var recdHtml = ""
        var length = data.recdBooks.length, i;
        for(i= 0; i< length; i++) {
            recdHtml = recdHtml+
                "<dl class=\"recdBook\">\n" +
                "            <dt>\n" +
                "                <a href=\"https://book.douban.com/subject/"+data.recdBooks[i].id+"\" onclick=\"moreurl(this, {'total': 10, 'clicked': '30143057', 'pos': 0, 'identifier': 'book-rec-books'})\">" +
                "                <img class=\"m_sub_img\" src=\""+data.recdBooks[i].image+"\"></a>\n" +
                "            </dt>\n" +
                "            <dd>\n" +
                "                <a href=\"https://book.douban.com/subject/"+data.recdBooks[i].id+" \" onclick=\"moreurl(this, {'total': 10, 'clicked': '30143057', 'pos': 0, 'identifier': 'book-rec-books'})\" class=\"\">\n"
                +                   data.recdBooks[i].title+
                "                </a>\n" +
                "                <button type=\"button\" class=\"btn-xs btn-info\">"+data.recdBooks[i].rating+"分</button>\n" +
                "            </dd>\n" +
                "</dl>"
            if ((i+1)%5==0){
                recdHtml = recdHtml+"<dl class=\"clear\"></dl>";
            }
        }
        if(i == 0) {
            recdHtml="<h3>该用户没有看过的书籍</h3>"
        }
        $(".recommentBooks").html(recdHtml)
    }, 'json');

    formatSparkRecd();
}

/*spark 推荐书籍*/
function formatSparkRecd() {
    var userId = $("#userId").val();
    $.get("/txt", {
        num:"50",
        userId: userId,

    }, function (data) {
        $(".sparkRecd").html("");
        var recdHtml = ""
        var length = data.recdBooks.length, i;
        for (i = 0; i < length; i++) {
            recdHtml = recdHtml +
                "<dl class=\"recdBook\">\n" +
                "            <dt>\n" +
                "                <a href=\"https://book.douban.com/subject/" + data.recdBooks[i].id + "\" onclick=\"moreurl(this, {'total': 10, 'clicked': '30143057', 'pos': 0, 'identifier': 'book-rec-books'})\">" +
                "                <img class=\"m_sub_img\" src=\"" + data.recdBooks[i].image + "\"></a>\n" +
                "            </dt>\n" +
                "            <dd>\n" +
                "                <a href=\"https://book.douban.com/subject/" + data.recdBooks[i].id + " \" onclick=\"moreurl(this, {'total': 10, 'clicked': '30143057', 'pos': 0, 'identifier': 'book-rec-books'})\" class=\"\">\n"
                + data.recdBooks[i].title +
                "                </a>\n" +
                "                <button type=\"button\" class=\"btn-xs btn-info\">" + data.recdBooks[i].rating + "分</button>\n" +
                "            </dd>\n" +
                "</dl>"
            if ((i + 1) % 5 == 0) {
                recdHtml = recdHtml + "<dl class=\"clear\"></dl>";
            }
        }
        if (i == 0) {
            recdHtml = "<h3>该用户没有看过的书籍</h3>"
        }
        $(".sparkRecd").html(recdHtml)
    }, 'json');
}
