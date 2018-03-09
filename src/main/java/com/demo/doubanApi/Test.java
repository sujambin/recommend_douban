package com.demo.doubanApi;

import com.demo.common.model.Book;
import com.google.gson.Gson;
import com.jfinal.json.FastJson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

public class Test {

    private final static String apiUrl = "https://api.douban.com/v2/book/";

    public static List<Record> getBooks(){
        return Db.find("select distinct bookId from rating");
    }
    //将Json数据解析成相应的映射对象
    public static <T> T parseJsonWithGson(String jsonData, Class<T> type) {
        Gson gson = new Gson();
        T result = gson.fromJson(jsonData, type);
        return result;
    }

    /**
     * json转换成相应的类型
     * @author Jambin
     * @date 2017-7-13
     * @return T
     * @version
     */
    public static <T> T  getJson(String jsonString, Class<T> type){
        return FastJson.getJson().parse(jsonString, type);
    }

    public static void main(String[] args) {

        String json = "{\"rating\":{\"max\":10,\"numRaters\":661,\"average\":\"8.8\",\"min\":0},\"subtitle\":\" The Little Star Dweller \",\"author\":[\"奈良美智\"],\"pubdate\":\"20041101\",\"tags\":[{\"count\":684,\"name\":\"奈良美智\",\"title\":\"奈良美智\"},{\"count\":388,\"name\":\"绘本\",\"title\":\"绘本\"},{\"count\":238,\"name\":\"日本\",\"title\":\"日本\"},{\"count\":170,\"name\":\"小星星通信\",\"title\":\"小星星通信\"},{\"count\":78,\"name\":\"繪本\",\"title\":\"繪本\"},{\"count\":68,\"name\":\"自傳\",\"title\":\"自傳\"},{\"count\":33,\"name\":\"插畫\",\"title\":\"插畫\"},{\"count\":28,\"name\":\"私藏书\",\"title\":\"私藏书\"}],\"origin_title\":\"\",\"image\":\"https://img3.doubanio.com\\/mpic\\/s1308125.jpg\",\"binding\":\"12 \\\" x 9 \\\"\",\"translator\":[],\"catalog\":\"\\r\\n\",\"pages\":\"168頁\",\"images\":{\"small\":\"https://img3.doubanio.com\\/spic\\/s1308125.jpg\",\"large\":\"https://img3.doubanio.com\\/lpic\\/s1308125.jpg\",\"medium\":\"https://img3.doubanio.com\\/mpic\\/s1308125.jpg\"},\"alt\":\"https:\\/\\/book.douban.com\\/subject\\/1291261\\/\",\"id\":\"1291261\",\"publisher\":\"大塊文化\",\"isbn10\":\"9867600827\",\"isbn13\":\"9789867600820\",\"title\":\"小星星通信\",\"url\":\"https:\\/\\/api.douban.com\\/v2\\/book\\/1291261\",\"alt_title\":\"\",\"author_intro\":\"奈良美智（NARA YOSHITOMO），出生於1959年12月5日，日本青森縣弘前市人。是日本現代美術界極具影響力的畫家。1981~1988年在愛知縣立藝術大學和研究所專攻美術。28歲隻身前往德國留學，創作至今，他的作品不時呈現年幼的記憶與現實經歷；數十年來，他以繪畫孩子和動物為創作主題。他的作品對抗傳統與權威，這種風格的形成與他一生摯愛的搖滾樂脫不了關係。奈良美智的作品有漫畫，也有動畫，他筆下的小女孩，既不友善卻又很寂寞的眼神，得到大眾的喝采與喜愛，也在國際間獲得注目，許多作品更被世界各國美術館收藏。\",\"summary\":\"奈良美智 \\r\\n出生於1959年12月5日，日本青森縣弘前市人。是日本現代美術界極具影響力的畫家。1981~1988年在愛知縣立藝術大學和研究所專攻美術。28歲隻身前往德國留學，創作至今，他的作品不時呈現年幼的記憶與現實經歷；數十年來，他以繪畫孩子和動物為創作主題。他的作品對抗傳統與權威，這種風格的形成與他一生摯愛的搖滾樂脫不了關係。奈良美智的作品有漫畫，也有動畫，他筆下的小女孩，既不友善卻又很寂寞的眼神，得到大眾的喝采與喜愛，也在國際間獲得注目，許多作品更被世界各國美術館收藏。 \\r\\n   第一次看見奈良美智的畫，他畫的只是一個邪惡的大眼娃娃，卻讓我內心震撼，這位畫家是誰？\\r\\n怎麼可以畫出讓觀眾看出你我在其中的畫？後來知道這位畫家叫奈良美智，他是日本男生，\\r\\n當時他住在德國，畫過很多動物和小孩……其他，一無所知，就像個謎一樣。\\r\\n\\r\\n後來讀過幾本他的日文書，大量圖片，少少的文字，這個人對我還是一個謎。\\r\\n再後來，在日文雜誌上看到介紹他的文字，說他是受到世界尊敬的100個日本人之一，這真是十分難得。\\r\\n拜時代進步之賜，幾年後的今天，奈良美智受邀到台灣辦畫展，身為編輯，我竟有榮幸編到他的書，雖然只是翻譯出版，卻也夠樂翻天了。《小星星通信》堪稱奈良美智的前傳，一般傳記大都以四平八穩的方式敘述生平，這本書很不一樣，加入大量影像圖片，在一頁一頁的閱讀中，我得以抽絲剝繭，逐漸解開畫家之謎。\\r\\n這位畫家不斷地流浪，從故鄉青森到東京又到愛知縣求學，到歐洲旅行，到德國取經，奈良美智在流浪中不斷地畫畫，從畫畫中觀照自己的內心。這本書不是一本天才的傳記，從這本書中，我們看見了藝術家的摸索、對藝術的思索與自我的努力；書中奈良美智沒有陳述太多私事，他把自己一直以來關注的文化層面做了簡單的說明，音樂、電影、畫畫……這些是他投入最多和最喜愛的事物。《小星星通信》介紹了奈良美智在各個時期聽的音樂，他的旅行，他的感動……\\r\\n這是一位多情的藝術家對世界的告白。奈良美智很害羞，本書是唯一能夠在作品之外，更了解他內心世界的一個入口。\",\"price\":\"NT$450\"}\n";

        Book book = getJson(json, Book.class);
//        BookJson book2 = parseJsonWithGson(json, BookJson.class);
        System.out.println(book.toString());
//        System.out.println(JsonKit.toJson(book2));

        System.out.println();
        System.out.println();
        System.out.println();
//        System.out.println(json);
    }

}
