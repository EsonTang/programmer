package com.prize.left.page.model;

/**
 * 百度电影相关常量定义
 */
public class MovieConfigConstant {

    /**
     * 展示电影首页或者获取电影首页数据
     * 所需参数： 
     * XlifeCallback:结果回调 
     * @param bundle action:operateOnShowMovieList 
     * ConfigConstant.CITY_ID_KEY:城市ID 不能为空 
     * ConfigConstant.OPTION_KEY:表示是否获取数据还是直接展示2级页面
     */
    public static final String ACTION_OPERATE_MOVIE_PAGE = "operateMoviePage";

    /**
     * 获取在映电影的列表
     * 所需参数： 
     * XlifeCallback:结果回调 
     * @param bundle action:operateOnShowMovieList 
     * ConfigConstant.CITY_ID_KEY:城市ID 不能为空 
     * provider_id:数据来源ID,允许为空，默认返回百度电影的数据
     * cinema_id: 影院ID,允许为空，此参数依赖于provider_id的存在 
     * page_no: 获取数据页码，允许为空，起始值为1，默认值取1
     * page_size:获取每页数量，允许为空，请保证在连贯的分页操作中，该字段必须保持一致 默认值取10，若设置为0，则不分页返回所有数据
     * ConfigConstant.OPTION_KEY:表示是否获取数据还是直接展示2级页面
     */
    public static final String ACTION_OPERATE_ONSHOW_MOVIE_LIST = "operateOnShowMovieList";

    /**
     * 获取将映电影的列表
     * 所需参数： 
     * XlifeCallback:结果回调 
     * ConfigConstant.CITY_ID_KEY:城市ID 不能为空 
     * provider_id:数据来源ID,允许为空，默认返回百度电影的数据 
     * cinema_id: 影院ID,允许为空，此参数以来provider_id的存在 
     * page_on: 获取数据页码，允许为空，起始值为1，默认值取1
     * page_size:获取每页数量，允许为空，请保证在连贯的分页操作中，该字段必须保持一致 默认值取10，若设置为0，则不分页返回所有数据
     * ConfigConstant.OPTION_KEY:表示是否获取数据还是直接展示2级页面
     */
    public static final String ACTION_OPERATE_UPCOME_MOVIE_LIST = "operateUpComeMovieList";

    /**
     * 展示某部电影详细信息页面或返回具体信息数据 
     * 所需参数： XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 MovieConfigConstant.KET_MOVIE_ID:影片ID 不能为空
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * ConfigConstant.OPTION_KEY:表示是否获取数据还是直接展示2级页面
     */
    public static final String ACTION_OPERATE_MOVIE_DETAIL = "operateMovieDetail";

    /**
     * 展示某部电影排片信息页面或返回排片信息数据
     * 所需参数： 
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * MovieConfigConstant.KET_MOVIE_ID:影片ID 不能为空
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * page_no: 获取数据页码，允许为空，起始值为1，默认值取1
     * page_size:获取每页数量，允许为空，请保证在连贯的分页操作中，该字段必须保持一致 默认值取10，若设置为0，则不分页返回所有数据
     * MovieConfigConstant.KEY_PAGE_NO：当前数据页码，允许为空
     * MovieConfigConstant.KEY_PAGE_SIZE：每页数据数量,允许为空
     */
    public static final String ACTION_OPERATE_MOVIE_SCHEDULE = "operateMovieSchedule";

    /**
     * 展示某部电影的评价列表页面或者获取数据  
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * MovieConfigConstant.KET_MOVIE_ID:影片ID 不能为空 
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * MovieConfigConstant.KEY_PAGE_NO：当前数据页码，允许为空
     * MovieConfigConstant.KEY_PAGE_SIZE：每页数据数量,允许为空
     */
    public static final String ACTION_OPERATE_MOVIE_COMMENT = "operateMovieComment";

    /**
     * 展示影院列表页面或者获取数据  
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * provider_id:数据来源ID,允许为空，默认返回百度电影的数据
     * MovieConfigConstant.KEY_PAGE_NO：当前数据页码，允许为空
     * MovieConfigConstant.KEY_PAGE_SIZE：每页数据数量,允许为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     */
    public static final String ACTION_OPERATE_CINEMA_LIST = "operateCinemaList";

    /**
     * 展示影院评价页面或者获取数据  
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * MovieConfigConstant.KEY_CINEMA_ID：影院id，表示具体影院，不允许为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     */
    public static final String ACTION_OPERATE_CINEMA_DETAIL = "operateCinemaDetatil";

    /**
     * 展示某部电影的评价列表页面或者获取数据  
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * MovieConfigConstant.KEY_CINEMA_ID:影院ID 不能为空 
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * MovieConfigConstant.KEY_PAGE_NO：当前数据页码，允许为空
     * MovieConfigConstant.KEY_PAGE_SIZE：每页数据数量,允许为空
     */
    public static final String ACTION_OPERATE_CINEMA_COMMENT = "operateCinemaComment";

    /**
     * 展示某影院的电影排片安排  
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * MovieConfigConstant.KEY_CINEMA_ID:影院ID 不能为空 
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * MovieConfigConstant.KEY_PAGE_NO：当前数据页码，允许为空
     * MovieConfigConstant.KEY_PAGE_SIZE：每页数据数量,允许为空
     */
    public static final String ACTION_OPERATE_CINEMA_SCHEDULE = "operateCinemaSchedule";

    /**
     * 获取影院的过滤条件信息
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * provider_id:数据来源ID,允许为空，默认返回百度电影的数据
     */
    public static final String ACTION_GET_CINEMA_FILTER = "getCinemaFilter";

    /**
     * 获取影院场次的选座详情
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * MovieConfigConstant.KEY_CINEMA_ID:影院ID 不能为空 
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * seq_id:场次，不能为空
     * datestamp:场次所在日期的时间戳,不允许为空
     * second_from:场次信息中的second_from字段
     * MovieConfigConstant.KET_MOVIE_ID:影片ID 不能为空
     */
    public static final String ACTION_OPERATE_SEAT = "operateSeat";

    /**
     * 创建订单
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * MovieConfigConstant.KEY_CINEMA_ID:影院ID 不能为空 
     * seq_id:场次，不能为空
     * seat_info:选中的座位的信息
     * MovieConfigConstant.KET_MOVIE_ID:影片ID 不能为空
     */
    public static final String ACTION_PLACE_ORDER = "placeOrder";

    /**
     * 展示订单列表或者获取订单数据
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * status: 订单类型 0 全部，1 待付款，2 已付款，3 出票成功，4 退款中，5 已退款，
     *                6 已失效，7 已观看，默认返回全部类型订单
     * MovieConfigConstant.KEY_PAGE_NO：当前数据页码，允许为空
     * MovieConfigConstant.KEY_PAGE_SIZE：每页数据数量,允许为空
     */
    public static final String ACTION_OPERATE_MOVIE_ORDER = "operateMovieOrder";

    /**
     * 展示订单详情页面或者获取订单详情数据
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * order_id:订单id，不允许为空
     */
    public static final String ACTION_OPERATE_MOVIE_ORDER_DETAIL = "operateMovieOrderDetail";

    /**
     * 删除订单
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * order_id:订单id，不允许为空
     */
    public static final String ACTION_DELETE_MOVIE_ORDER = "deleteMovieOrder";
    
    /**
     * 确认订单
     * 所需参数：
     * XlifeCallback:结果回调 
     * ConfigConstant.KET_CITY_ID:城市ID 不能为空
     * ConfigConstant.KET_OPTION:表示是否获取数据还是直接展示2级页面 
     * provider_id:数据来源ID,不允许为空，默认返回百度电影的数据
     * order_id:订单id，不允许为空
     */
    public static final String ACTION_OPERATE_MOVIE_ORDER_CONFIRM = "operateMovieOrderConfirm";

    /**
     * 对应影院ID
     */
    public static final String KEY_CINEMA_ID = "cinema_id";

    /**
     * 对应影片ID
     */
    public static final String KEY_MOVIE_ID = "movie_id";

    /**
     * 对应数据页码
     */
    public static final String KEY_PAGE_NO = "page_no";

    /**
     * 对应每页数量
     */
    public static final String KEY_PAGE_SIZE = "page_size";

    /**
     * 日期
     */
    public static final String KEY_DATE = "date";

    /**
     * 场次
     */
    public static final String KEY_SEQ_ID = "seq_id";

    /**
     * 场次所在日期的时间戳
     */
    public static final String KEY_DATE_STAMP = "datestamp";

    /**
     * 场次信息中的second_from字段
     */
    public static final String KEY_SECOND_FROM = "second_from";

    /**
     * 场次信息中的second_from字段
     */
    public static final String KEY_DISTRICT_ID = "district_id";

    /**
     * 场次信息中的second_from字段
     */
    public static final String KEY_AREA_ID = "area_id";

    /**
     * 场次信息中的second_from字段
     */
    public static final String KEY_SUBWAY_LINE_ID = "subway_line_id";

    /**
     * 场次信息中的second_from字段
     */
    public static final String KEY_SUBWAY_STATION_ID = "subway_station_id";

    /**
     * 场次信息中的second_from字段
     */
    public static final String KEY_BRAND_ID = "brand_id";

    /**
     * 场次信息中的second_from字段
     */
    public static final String KEY_SORT_TYPE = "sort_type";

    /**
     * 座位信息
     */
    public static final String KEY_SEAT_INFO = "seat_info";

    /**
     * 订单id
     */
    public static final String KEY_ORDER_ID = "order_id";

    /**
     * 订单类型
     */
    public static final String KEY_STATUS = "status";
}
