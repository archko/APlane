package com.me.microblog.core.sina;

import android.text.TextUtils;
import com.me.microblog.WeiboException;
import com.me.microblog.bean.PlacePoi;
import com.me.microblog.bean.SStatusData;
import com.me.microblog.bean.Status;
import com.me.microblog.bean.User;
import com.me.microblog.core.AbsApiImpl;
import com.me.microblog.core.WeiboParser;
import com.me.microblog.core.abs.IPlaceApi;
import com.me.microblog.http.PostParameter;
import com.me.microblog.util.WeiboLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: archko Date: 13-1-28 Time: 下午7:15
 * @description:
 */
public class SinaPlaceApi extends AbsApiImpl implements IPlaceApi {

    public static final String TAG = "SinaPlaceApi";

    //--------------------- 位置服务 ---------------------

    /**
     * 获取当前登录用户与其好友的位置动态
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * since_id 	false 	int64 	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
     * max_id 	false 	int64 	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * count 	false 	int 	单页返回的记录条数，最大为50，默认为20。
     * page 	false 	int 	返回结果的页码，默认为1。
     * type 	false 	int 	关系过滤，0：仅返回关注的，1：返回好友的，默认为0。
     *
     * 只有total_number，使用gzip压缩返回的数据有问题，前面没有'{'
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getPlaceFriendTimeline(long sinceId, long maxId, int count, int page, int type)
        throws WeiboException {
        String urlString = getBaseUrl() + "place/friends_timeline.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (maxId > 0) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (type > 0) {
            pair = new PostParameter("type", String.valueOf(type));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取某个用户的位置动态
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	true 	int64 	需要查询的用户ID。
     * since_id 	false 	int64 	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
     * max_id 	false 	int64 	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * count 	false 	int 	单页返回的记录条数，最大为50，默认为20。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * 只有total_number
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getPlaceUserFriendTimeline(long sinceId, long maxId, int count, int page)
        throws WeiboException {
        String urlString = getBaseUrl() + "place/user_timeline.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (maxId > 0) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult(TAG, "rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取某个位置地点的动态
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要查询的POI点ID。
     * since_id 	false 	string 	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
     * max_id 	false 	string 	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * count 	false 	int 	单页返回的记录条数，最大为50，默认为20。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * 只有total_number
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getPlacePoiFriendTimeline(String poiid, long sinceId, long maxId, int count,
        int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/poi_timeline.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("poiid", poiid);
        nvps.add(pair);

        if (sinceId > 0) {
            pair = new PostParameter("since_id", String.valueOf(sinceId));
            nvps.add(pair);
        }

        if (maxId > 0) {
            pair = new PostParameter("max_id", String.valueOf(maxId));
            nvps.add(pair);
        }

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取某个位置周边的动态
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * lat 	true 	float 	纬度。有效范围：-90.0到+90.0，+表示北纬。
     * long 	true 	float 	经度。有效范围：-180.0到+180.0，+表示东经。
     * range 	false 	int 	搜索范围，单位米，默认2000米，最大11132米。
     * starttime 	false 	int 	开始时间，Unix时间戳。
     * endtime 	false 	int 	结束时间，Unix时间戳。
     * sort 	false 	int 	排序方式。默认为0，按时间排序；为1时按与中心点距离进行排序。
     * count 	false 	int 	单页返回的记录条数，最大为50，默认为20。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * offset 	false 	int 	传入的经纬度是否是纠偏过，0：没纠偏、1：纠偏过，默认为0。
     *
     * 只有total_number,还有其它数据"states":[{"id":"3478567679549904","state":0},{"id":"3478567696947563","state":0},]}
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getPlaceNearbyFriendTimeline(double lat, double llong, int count, int page)
        throws WeiboException {
        String urlString = getBaseUrl() + "place/nearby_timeline.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("lat", String.valueOf(lat));
        nvps.add(pair);

        pair = new PostParameter("long", String.valueOf(llong));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 根据ID获取动态的详情
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * id 	true 	int64 	需要获取的动态ID。
     *
     * 与place/nearby_timeline.json相关，动态id从上面取得，其实微博的内容一样，不必再获取一次。
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public Status getPlaceStatusShow(long id) throws WeiboException {
        String urlString = getBaseUrl() + "place/statuses/show.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("id", String.valueOf(id));
        nvps.add(pair);

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parseStatus(rs);
    }

    /**
     * 获取LBS位置服务内的用户信息
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	true 	int64 	需要查询的用户ID。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * @return {"uid": "1315982102","checkin_num": 2,"tip_num": 3,"photo_num": 1,"todo_num": 0,"geo_statuses_num": 0}
     * @throws com.me.microblog.WeiboException
     */
    public void getPlaceUser(long id) throws WeiboException {
        String urlString = getBaseUrl() + "place/users/show.json";

        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;
        pair = new PostParameter("uid", String.valueOf(id));
        nvps.add(pair);

        String rs = get(urlString, true, nvps);
        WeiboLog.printResult("rs:" + rs);
    }

    /**
     * 获取用户的照片列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	true 	int64 	需要查询的用户ID。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * return 文档中返回的是微博列表一样的数据,多了"total_number": 20
     */
    public SStatusData<Status> getUserPhotos(long uid, int count, int page, int base_app)
        throws WeiboException {
        String urlString = getBaseUrl() + "place/users/photos.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();
        PostParameter pair;

        pair = new PostParameter("uid", String.valueOf(uid));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("getUserPhotos:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取用户的照片列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	true 	int64 	需要查询的用户ID。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * 只有total_number
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getUserPhotos(long uid, int count, int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/users/photos.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("uid", String.valueOf(uid));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取用户的点评列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * uid 	true 	int64 	需要查询的用户ID。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * 只有total_number ,且带有geo，annotations
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getPlaceUserTips(long uid, int count, int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/users/tips.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("uid", String.valueOf(uid));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取地点详情
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要查询的POI地点ID。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * 只有total_number ,且带有geo，annotations
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public PlacePoi getPlacePoi(String poiid) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/show.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("poiid", (poiid));
        nvps.add(pair);

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parsePlacePoi(rs);
    }

    /**
     * 获取在某个地点签到的人的列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要查询的POI地点ID。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * @return 返回的数据多了"total_number": 200
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<User> getPlacesUsers(String poiid, int count, int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/users.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("poiid", (poiid));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.getUserObjs2(rs);
    }

    /**
     * 获取地点点评列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要查询的POI地点ID。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * sort 	false 	int 	排序方式，0：按时间、1：按热门，默认为0，目前只支持0。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * 只有total_number ,且带有geo，annotations
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getPoiTips(String poiid, int count, int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/tips.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("poiid", (poiid));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 获取地点照片列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要查询的POI地点ID。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * sort 	false 	int 	排序方式，0：按时间、1：按热门，默认为0，目前只支持0。
     * base_app 	false 	int 	是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     *
     * 只有total_number ,且带有geo，annotations
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<Status> getPlacePhotos(String poiid, int count, int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/photos.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("poiid", (poiid));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 按省市查询地点
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * keyword 	true 	string 	查询的关键词，必须进行URLencode。
     * city 	false 	string 	城市代码，默认为全国搜索。
     * category 	false 	string 	查询的分类代码，取值范围见：分类代码对应表。
     * page 	false 	int 	返回结果的页码，默认为1。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     *
     * 只有total_number ,且带有geo，annotations
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<PlacePoi> getPlaceSearch(String keyword, String city, String category, int count, int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/search.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("keyword", keyword);
        nvps.add(pair);

        if (! TextUtils.isEmpty(city)) {
            pair = new PostParameter("city", city);
            nvps.add(pair);
        }

        if (! TextUtils.isEmpty(city)) {
            pair = new PostParameter("category", category);
            nvps.add(pair);
        }

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parsePlacePois(rs);
    }

    /**
     * 获取地点分类
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * pid 	false 	int 	父分类ID，默认为0。
     * flag false 	int 	是否返回全部分类，0：只返回本级下的分类，1：返回全部分类，默认为0。
     *
     * [{"id": "19","name": "出行住宿","pid": "0"},...]
     *
     * @return
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<PlacePoi> getPlaceCategory(int pid, int flag) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/category.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        if (pid > 0) {
            pair = new PostParameter("pid", String.valueOf(pid));
            nvps.add(pair);
        }

        if (flag > 0) {
            pair = new PostParameter("flag", String.valueOf(flag));
            nvps.add(pair);
        }

        String rs = get(urlString, true, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.parsePlacePois(rs);
    }

    /**
     * 获取附近地点
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * lat 	true 	float 	纬度，有效范围：-90.0到+90.0，+表示北纬。
     * long 	true 	float 	经度，有效范围：-180.0到+180.0，+表示东经。
     * range 	false 	int 	查询范围半径，默认为2000，最大为10000，单位米。
     * q 	false 	string 	查询的关键词，必须进行URLencode。
     * category 	false 	string 	查询的分类代码，取值范围见：分类代码对应表。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * sort 	false 	int 	排序方式，0：按权重，默认为0。
     * offset 	false 	int 	传入的经纬度是否是纠偏过，0：没纠偏、1：纠偏过，默认为0
     *
     * return 文档中返回的是微博列表一样的数据,多了"total_number": 20
     */
    public SStatusData<PlacePoi> getPlaceNearbyPoi(double lat, double llong, int range, int count, int page)
        throws WeiboException {
        String urlString = getBaseUrl() + "place/nearby/pois.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();
        PostParameter pair;

        pair = new PostParameter("lat", String.valueOf(lat));
        nvps.add(pair);

        pair = new PostParameter("long", String.valueOf(llong));
        nvps.add(pair);

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("getPlaceNearbyPoi:" + rs);
        return WeiboParser.parsePlacePois(rs);
    }

    /**
     * 获取附近发位置微博的人
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * lat 	true 	float 	纬度，有效范围：-90.0到+90.0，+表示北纬。
     * long 	true 	float 	经度，有效范围：-180.0到+180.0，+表示东经。
     * range 	false 	int 	查询范围半径，默认为2000，最大为11132，单位米。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * starttime 	false 	int64 	开始时间，Unix时间戳。
     * endtime 	false 	int64 	结束时间，Unix时间戳。
     * sort 	false 	int 	排序方式，0：按时间、1：按距离，默认为0。
     * offset 	false 	int 	传入的经纬度是否是纠偏过，0：没纠偏、1：纠偏过，默认为0。
     *
     * @return 返回的数据多了"total_number": 200
     * @throws com.me.microblog.WeiboException
     */
    public SStatusData<User> getPlacesNearbyUsers(double lat, double llong, int range, int count, int page) throws WeiboException {
        String urlString = getBaseUrl() + "place/nearby/users.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();

        PostParameter pair;

        pair = new PostParameter("lat", String.valueOf(lat));
        nvps.add(pair);
        pair = new PostParameter("long", String.valueOf(llong));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (range > 0) {
            pair = new PostParameter("range", String.valueOf(range));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("rs:" + rs);
        return WeiboParser.getUserObjs2(rs);
    }

    /**
     * 获取用户的照片列表
     * GET 需要登录	访问级别：普通接口  频次限制：是
     * lat 	true 	float 	纬度，有效范围：-90.0到+90.0，+表示北纬。
     * long 	true 	float 	经度，有效范围：-180.0到+180.0，+表示东经。
     * range 	false 	int 	查询范围半径，默认为500，最大为11132，单位米。
     * count 	false 	int 	单页返回的记录条数，默认为20，最大为50。
     * page 	false 	int 	返回结果的页码，默认为1。
     * starttime 	false 	int64 	开始时间，Unix时间戳。
     * endtime 	false 	int64 	结束时间，Unix时间戳。
     * sort 	false 	int 	排序方式，0：按时间、1：按距离，默认为0。
     * offset 	false 	int 	传入的经纬度是否是纠偏过，0：没纠偏、1：纠偏过，默认为0。
     * <p/>
     * return 文档中返回的是微博列表一样的数据,多了"total_number": 20
     */
    public SStatusData<Status> getNearbyPhotos(double lat, double llong, int range, int count, int page)
        throws WeiboException {
        String urlString = getBaseUrl() + "place/nearby/photos.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();
        PostParameter pair;

        pair = new PostParameter("lat", String.valueOf(lat));
        nvps.add(pair);

        pair = new PostParameter("long", String.valueOf(llong));
        nvps.add(pair);

        if (count > 0) {
            pair = new PostParameter("count", String.valueOf(count));
            nvps.add(pair);
        }

        if (page > 0) {
            pair = new PostParameter("page", String.valueOf(page));
            nvps.add(pair);
        }

        if (range > 0) {
            pair = new PostParameter("range", String.valueOf(range));
            nvps.add(pair);
        }

        String rs = get(urlString, false, nvps);
        WeiboLog.printResult("getNearbyPhotos:" + rs);
        return WeiboParser.parseStatuses2(rs);
    }

    /**
     * 签到同时可以上传一张图片
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要签到的POI地点ID。
     * status 	true 	string 	签到时发布的动态内容，必须做URLencode，内容不超过140个汉字。
     * pic 	false 	binary 	需要上传的图片，仅支持JPEG、GIF、PNG格式，图片大小小于5M。
     * public 	false 	int 	是否同步到微博，1：是、0：否，默认为0。
     *
     * return 文档中返回的是微博列表一样的数据,多了"total_number": 20
     */
    public Status addCheckin(String poiid, String status) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/add_checkin.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();
        PostParameter pair;

        pair = new PostParameter("poiid", poiid);
        nvps.add(pair);

        pair = new PostParameter("status", status);
        nvps.add(pair);

        String rs = post(urlString, true, nvps);
        WeiboLog.printResult(TAG, "addCheckin:" + rs);
        return WeiboParser.parseStatus(rs);
    }

    /**
     * 添加照片
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要添加照片的POI地点ID。
     * status 	true 	string 	签到时发布的动态内容，必须做URLencode，内容不超过140个汉字。
     * pic 	false 	binary 	需要上传的图片，仅支持JPEG、GIF、PNG格式，图片大小小于5M。
     * public 	false 	int 	是否同步到微博，1：是、0：否，默认为0。
     * 请求必须用POST方式提交，并且注意采用multipart/form-data编码方式；
     * <p/>
     * return 文档中返回的是微博列表一样的数据,多了"total_number": 20
     */
    public void addPhotos(String poiid, String status, String pic_path, int public_to)
        throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/add_photo.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();
        PostParameter pair;

        pair = new PostParameter("poiid", poiid);
        nvps.add(pair);

        pair = new PostParameter("status", status);
        nvps.add(pair);

        if (public_to > 0) {
            pair = new PostParameter("public", String.valueOf(public_to));
            nvps.add(pair);
        }

        String rs = post(urlString, true, nvps);
        WeiboLog.printResult(TAG, "addPhotos:" + rs);
    }

    /**
     * 添加点评
     * POST 需要登录	访问级别：普通接口  频次限制：是
     * poiid 	true 	string 	需要点评的POI地点ID。
     * status 	true 	string 	点评时发布的动态内容，必须做URLencode，内容不超过140个汉字。
     * public 	false 	int 	是否同步到微博，1：是、0：否，默认为0。
     *
     * return 微博实体
     */
    public Status addPlaceTip(String poiid, String status, int public_to) throws WeiboException {
        String urlString = getBaseUrl() + "place/pois/add_tip.json";
        List<PostParameter> nvps = new ArrayList<PostParameter>();
        PostParameter pair;

        pair = new PostParameter("poiid", poiid);
        nvps.add(pair);

        pair = new PostParameter("status", status);
        nvps.add(pair);

        if (public_to > 0) {
            pair = new PostParameter("public", String.valueOf(public_to));
            nvps.add(pair);
        }

        String rs = post(urlString, false, nvps);
        WeiboLog.printResult(TAG, "addPlaceTip:" + rs);
        return WeiboParser.parseStatus(rs);
    }
}
