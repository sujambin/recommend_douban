

$(function(){
   getRecdBooks();
});






/*加载推荐爱的书本书本*/
function getRecdBooks(curpage_) {
    index_state.page = parseInt(curpage_);
    index_state.state = 1;
    index_state.type = '0';
    index_state.temp = "所有书籍";
    $.post("/management/getBooksTitleByPage", {
        curpage: curpage_,
        pagesize: 8
    }, function(data) {
        /*到时候多种书籍的时候，加载书籍的函数要分离*/
        var length = data.tbody.length, i;
        for(i= 0; i< length; i++) {
            var aim = ".recdBook:eq("+ i+ ") .thumbnail ";
            $(aim + "img").attr('src', 'images/'+data.tbody[i].id+'.jpg');
            $(aim + ".caption h3:eq(0)").hide().html("《"+ data.tbody[i].name+ "》").fadeIn().attr('title',data.tbody[i].name);
            if(data.tbody[i].borrower) {
                $(aim + ".caption p a.borrow").attr('data-id', data.tbody[i].id).attr('disabled',true).off('click');
                $(aim + ".caption p.borrow-state").html("有人把这本书借走了");
            }else {
                $(aim + ".caption p.borrow-state").html(data.tbody[i].borrowerInfo);
                $(aim + ".caption p a.borrow").attr('data-id', data.tbody[i].id).attr('disabled',false).on('click',borrowBook);
            }

        }
        while(i++ < 8) {
            var aim = ".book:eq("+ i+ ") .thumbnail .caption ";
            $(aim + "img").attr('src', 'images/default.jpg');
            $(aim + "h3:eq(0)").hide().html("暂无").fadeIn();
            $(aim + ".caption p a.borrow").addClass('disabled');
        }
        index_state.pageCount = data.pagecount;
        pageDeal();
    }, 'json');
}