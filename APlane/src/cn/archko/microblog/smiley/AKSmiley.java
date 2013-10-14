package cn.archko.microblog.smiley;

import cn.archko.microblog.R;

import java.util.HashMap;

/**
 * User: archko Date: 12-9-27 Time: 上午9:21
 */
public final class AKSmiley {

    public static final HashMap<String, Integer> mSmileyMap=new HashMap<String, Integer>();

    static {
        mSmileyMap.put("[织]", Integer.valueOf(R.drawable.zz2_org));
        mSmileyMap.put("[神马]", Integer.valueOf(R.drawable.horse2_org));
        mSmileyMap.put("[浮云]", Integer.valueOf(R.drawable.fuyun_org));
        mSmileyMap.put("[给力]", Integer.valueOf(R.drawable.geili_org));
        mSmileyMap.put("[围观]", Integer.valueOf(R.drawable.wg_org));
        mSmileyMap.put("[威武]", Integer.valueOf(R.drawable.vw_org));
        mSmileyMap.put("[熊猫]", Integer.valueOf(R.drawable.panda_org));
        mSmileyMap.put("[兔子]", Integer.valueOf(R.drawable.rabbit_org));
        mSmileyMap.put("[奥特曼]", Integer.valueOf(R.drawable.otm_org));
        mSmileyMap.put("[囧]", Integer.valueOf(R.drawable.j_org));
        mSmileyMap.put("[互粉]", Integer.valueOf(R.drawable.hufen_org));
        mSmileyMap.put("[礼物]", Integer.valueOf(R.drawable.liwu_org));
        mSmileyMap.put("[呵呵]", Integer.valueOf(R.drawable.smilea_org));
        mSmileyMap.put("[嘻嘻]", Integer.valueOf(R.drawable.tootha_org));
        mSmileyMap.put("[哈哈]", Integer.valueOf(R.drawable.laugh));
        mSmileyMap.put("[可爱]", Integer.valueOf(R.drawable.tza_org));
        mSmileyMap.put("[可怜]", Integer.valueOf(R.drawable.kl_org));
        mSmileyMap.put("[挖鼻屎]", Integer.valueOf(R.drawable.kbsa_org));
        mSmileyMap.put("[吃惊]", Integer.valueOf(R.drawable.cj_org));
        mSmileyMap.put("[害羞]", Integer.valueOf(R.drawable.shamea_org));
        mSmileyMap.put("[挤眼]", Integer.valueOf(R.drawable.zy_org));
        mSmileyMap.put("[闭嘴]", Integer.valueOf(R.drawable.bz_org));
        mSmileyMap.put("[鄙视]", Integer.valueOf(R.drawable.bs2_org));
        mSmileyMap.put("[爱你]", Integer.valueOf(R.drawable.lovea_org));
        mSmileyMap.put("[泪]", Integer.valueOf(R.drawable.sada_org));
        mSmileyMap.put("[偷笑]", Integer.valueOf(R.drawable.heia_org));
        mSmileyMap.put("[亲亲]", Integer.valueOf(R.drawable.qq_org));
        mSmileyMap.put("[生病]", Integer.valueOf(R.drawable.sb_org));
        mSmileyMap.put("[太开心]", Integer.valueOf(R.drawable.mb_org));
        mSmileyMap.put("[懒得理你]", Integer.valueOf(R.drawable.ldln_org));
        mSmileyMap.put("[右哼哼]", Integer.valueOf(R.drawable.yhh_org));
        mSmileyMap.put("[左哼哼]", Integer.valueOf(R.drawable.zhh_org));
        mSmileyMap.put("[嘘]", Integer.valueOf(R.drawable.x_org));
        mSmileyMap.put("[衰]", Integer.valueOf(R.drawable.cry));
        mSmileyMap.put("[委屈]", Integer.valueOf(R.drawable.wq_org));
        mSmileyMap.put("[吐]", Integer.valueOf(R.drawable.t_org));
        //打哈欠]抱抱]",
        mSmileyMap.put("[怒]", Integer.valueOf(R.drawable.angrya_org));
        //疑问
        mSmileyMap.put("[馋嘴]", Integer.valueOf(R.drawable.cza_org));
        mSmileyMap.put("[拜拜]", Integer.valueOf(R.drawable.bb_org));
        mSmileyMap.put("[思考]", Integer.valueOf(R.drawable.sk_org));
        mSmileyMap.put("[汗]", Integer.valueOf(R.drawable.sweata_org));
        mSmileyMap.put("[困]", Integer.valueOf(R.drawable.sleepya_org));
        mSmileyMap.put("[睡觉]", Integer.valueOf(R.drawable.sleepa_org));
        mSmileyMap.put("[钱]", Integer.valueOf(R.drawable.money_org));
        mSmileyMap.put("[失望]", Integer.valueOf(R.drawable.sw_org));
        mSmileyMap.put("[酷]", Integer.valueOf(R.drawable.cool_org));
        mSmileyMap.put("[花心]", Integer.valueOf(R.drawable.hsa_org));
        mSmileyMap.put("[哼]", Integer.valueOf(R.drawable.hatea_org));
        mSmileyMap.put("[鼓掌]", Integer.valueOf(R.drawable.gza_org));
        mSmileyMap.put("[晕]", Integer.valueOf(R.drawable.dizzya_org));
        mSmileyMap.put("[悲伤]", Integer.valueOf(R.drawable.bs_org));
        mSmileyMap.put("[抓狂]", Integer.valueOf(R.drawable.crazya_org));
        //黑线,阴险
        mSmileyMap.put("[怒骂]", Integer.valueOf(R.drawable.nm_org));
        mSmileyMap.put("[心]", Integer.valueOf(R.drawable.hearta_org));
        mSmileyMap.put("[伤心]", Integer.valueOf(R.drawable.unheart));
        mSmileyMap.put("[猪头]", Integer.valueOf(R.drawable.pig));
        mSmileyMap.put("[ok]", Integer.valueOf(R.drawable.ok_org));
        mSmileyMap.put("[耶]", Integer.valueOf(R.drawable.ye_org));
        mSmileyMap.put("[good]", Integer.valueOf(R.drawable.good_org));
        mSmileyMap.put("[不要]", Integer.valueOf(R.drawable.no_org));
        mSmileyMap.put("[赞]", Integer.valueOf(R.drawable.z2_org));
        mSmileyMap.put("[来]", Integer.valueOf(R.drawable.come_org));
        mSmileyMap.put("[弱]", Integer.valueOf(R.drawable.sad_org));
        mSmileyMap.put("[蜡烛]", Integer.valueOf(R.drawable.lazu_org));
        mSmileyMap.put("[钟]", Integer.valueOf(R.drawable.clock_org));
        mSmileyMap.put("[蛋糕]", Integer.valueOf(R.drawable.cake));
        mSmileyMap.put("[话筒]", Integer.valueOf(R.drawable.m_org));

        /*mSmileyMap.put("粽子", Integer.valueOf(2130837847));
        mSmileyMap.put("风扇", Integer.valueOf(2130837858));
        mSmileyMap.put("花", Integer.valueOf(2130837644));
        mSmileyMap.put("足球", Integer.valueOf(2130837652));
        mSmileyMap.put("冰棍", Integer.valueOf(2130837687));
        mSmileyMap.put("红牌", Integer.valueOf(2130837732));
        mSmileyMap.put("阳光", Integer.valueOf(2130837773));
        mSmileyMap.put("黄牌", Integer.valueOf(2130837789));
        mSmileyMap.put("西瓜", Integer.valueOf(2130837797));
        mSmileyMap.put("闪", Integer.valueOf(2130837798));
        mSmileyMap.put("庆祝", Integer.valueOf(2130837799));
        mSmileyMap.put("啦啦", Integer.valueOf(2130837800));
        mSmileyMap.put("吼吼", Integer.valueOf(2130837801));
        mSmileyMap.put("宅", Integer.valueOf(2130837802));
        mSmileyMap.put("恐怖", Integer.valueOf(2130837803));
        mSmileyMap.put("哇哈哈", Integer.valueOf(2130837804));
        mSmileyMap.put("差得远呢", Integer.valueOf(2130837805));
        mSmileyMap.put("抽耳光", Integer.valueOf(2130837807));
        mSmileyMap.put("吵闹", Integer.valueOf(2130837808));
        mSmileyMap.put("得意", Integer.valueOf(2130837809));
        mSmileyMap.put("犯错", Integer.valueOf(2130837810));
        mSmileyMap.put("发嗲", Integer.valueOf(2130837811));
        mSmileyMap.put("尴尬", Integer.valueOf(2130837812));
        mSmileyMap.put("交给我吧", Integer.valueOf(2130837813));
        mSmileyMap.put("亲", Integer.valueOf(2130837814));
        mSmileyMap.put("奔泪", Integer.valueOf(2130837815));
        mSmileyMap.put("变脸色", Integer.valueOf(2130837816));
        mSmileyMap.put("路过", Integer.valueOf(2130837817));
        mSmileyMap.put("膜拜", Integer.valueOf(2130837818));
        mSmileyMap.put("怒吼", Integer.valueOf(2130837819));
        mSmileyMap.put("拍照", Integer.valueOf(2130837820));
        mSmileyMap.put("切", Integer.valueOf(2130837821));
        mSmileyMap.put("高兴", Integer.valueOf(2130837822));
        mSmileyMap.put("不倒翁", Integer.valueOf(2130837823));
        mSmileyMap.put("上火", Integer.valueOf(2130837824));
        mSmileyMap.put("必胜", Integer.valueOf(2130837825));
        mSmileyMap.put("不屑", Integer.valueOf(2130837826));
        mSmileyMap.put("抱枕", Integer.valueOf(2130837828));
        mSmileyMap.put("蹭", Integer.valueOf(2130837829));
        mSmileyMap.put("发奋", Integer.valueOf(2130837830));
        mSmileyMap.put("叹气", Integer.valueOf(2130837831));
        mSmileyMap.put("high", Integer.valueOf(2130837832));
        mSmileyMap.put("搞笑", Integer.valueOf(2130837833));
        mSmileyMap.put("喜欢", Integer.valueOf(2130837834));
        mSmileyMap.put("手足舞蹈", Integer.valueOf(2130837835));
        mSmileyMap.put("美好", Integer.valueOf(2130837836));
        mSmileyMap.put("无聊", Integer.valueOf(2130837837));
        mSmileyMap.put("yeah", Integer.valueOf(2130837839));
        mSmileyMap.put("晃", Integer.valueOf(2130837840));
        mSmileyMap.put("ss", Integer.valueOf(2130837841));
        mSmileyMap.put("白羊座", Integer.valueOf(2130837842));
        mSmileyMap.put("茶", Integer.valueOf(2130837844));
        mSmileyMap.put("电脑", Integer.valueOf(2130837845));
        mSmileyMap.put("钢琴", Integer.valueOf(2130837846));
        mSmileyMap.put("叶子", Integer.valueOf(2130837848));
        mSmileyMap.put("档案", Integer.valueOf(2130837849));
        mSmileyMap.put("工作", Integer.valueOf(2130837850));
        mSmileyMap.put("帽子", Integer.valueOf(2130837851));
        mSmileyMap.put("房子", Integer.valueOf(2130837852));
        mSmileyMap.put("酒", Integer.valueOf(2130837853));
        mSmileyMap.put("脚印", Integer.valueOf(2130837854));
        mSmileyMap.put("唱歌", Integer.valueOf(2130837855));
        mSmileyMap.put("左抱抱", Integer.valueOf(2130837856));
        mSmileyMap.put("狮子座", Integer.valueOf(2130837857));
        mSmileyMap.put("电影", Integer.valueOf(2130837860));
        mSmileyMap.put("音乐", Integer.valueOf(2130837861));
        mSmileyMap.put("巧克力", Integer.valueOf(2130837862));
        mSmileyMap.put("ss", Integer.valueOf(2130837864));
        mSmileyMap.put("电视机", Integer.valueOf(2130837865));
        mSmileyMap.put("酒壶", Integer.valueOf(2130837866));
        mSmileyMap.put("打针", Integer.valueOf(2130837867));
        mSmileyMap.put("自行车", Integer.valueOf(2130837868));
        mSmileyMap.put("haha", Integer.valueOf(2130837646));
        mSmileyMap.put("金牛", Integer.valueOf(2130837649));
        mSmileyMap.put("狮子", Integer.valueOf(2130837650));
        mSmileyMap.put("摩羯", Integer.valueOf(2130837651));
        mSmileyMap.put("水瓶", Integer.valueOf(2130837653));
        mSmileyMap.put("射手", Integer.valueOf(2130837654));
        mSmileyMap.put("双鱼", Integer.valueOf(2130837655));
        mSmileyMap.put("天蝎", Integer.valueOf(2130837656));
        mSmileyMap.put("最差", Integer.valueOf(2130837657));
        mSmileyMap.put("龇牙", Integer.valueOf(2130837658));
        mSmileyMap.put("处女", Integer.valueOf(2130837659));
        mSmileyMap.put("奋斗", Integer.valueOf(2130837660));
        mSmileyMap.put("汗了", Integer.valueOf(2130837760));
        mSmileyMap.put("鸭梨", Integer.valueOf(2130838031));
        mSmileyMap.put("激动", Integer.valueOf(2130837663));
        mSmileyMap.put("紧张", Integer.valueOf(2130837664));
        mSmileyMap.put("好可怜", Integer.valueOf(2130837665));
        mSmileyMap.put("哭", Integer.valueOf(2130837666));
        mSmileyMap.put("瞄", Integer.valueOf(2130837667));
        mSmileyMap.put("疲劳", Integer.valueOf(2130837668));
        mSmileyMap.put("色咪咪", Integer.valueOf(2130837669));
        mSmileyMap.put("生病了", Integer.valueOf(2130837670));
        mSmileyMap.put("生气", Integer.valueOf(2130837672));
        mSmileyMap.put("帅爆", Integer.valueOf(2130837673));
        mSmileyMap.put("微笑", Integer.valueOf(2130837674));
        mSmileyMap.put("有钱", Integer.valueOf(2130837675));
        mSmileyMap.put("逗号", Integer.valueOf(2130837676));
        mSmileyMap.put("钻石", Integer.valueOf(2130837677));
        mSmileyMap.put("沙尘暴", Integer.valueOf(2130837678));
        mSmileyMap.put("orz", Integer.valueOf(2130837679));
        mSmileyMap.put("洪水", Integer.valueOf(2130837680));
        mSmileyMap.put("揪耳朵", Integer.valueOf(2130837681));
        mSmileyMap.put("金牛座", Integer.valueOf(2130837682));
        mSmileyMap.put("欢欢", Integer.valueOf(2130837683));
        mSmileyMap.put("龙卷风", Integer.valueOf(2130837684));
        mSmileyMap.put("摩羯座", Integer.valueOf(2130837685));
        mSmileyMap.put("拳头", Integer.valueOf(2130837686));
        mSmileyMap.put("钻戒", Integer.valueOf(2130837688));
        mSmileyMap.put("下雨", Integer.valueOf(2130837689));
        mSmileyMap.put("右抱抱", Integer.valueOf(2130837690));
        mSmileyMap.put("热吻", Integer.valueOf(2130837691));
        mSmileyMap.put("闪电", Integer.valueOf(2130837692));
        mSmileyMap.put("哨子", Integer.valueOf(2130837693));
        mSmileyMap.put("手机", Integer.valueOf(2130837694));
        mSmileyMap.put("ss", Integer.valueOf(2130837695));
        mSmileyMap.put("水瓶座", Integer.valueOf(2130837696));
        mSmileyMap.put("星", Integer.valueOf(2130837697));
        mSmileyMap.put("ss", Integer.valueOf(2130837698));
        mSmileyMap.put("阳光", Integer.valueOf(2130837699));
        mSmileyMap.put("实习", Integer.valueOf(2130837700));
        mSmileyMap.put("双鱼", Integer.valueOf(2130837701));
        mSmileyMap.put("双子", Integer.valueOf(2130837702));
        mSmileyMap.put("电话", Integer.valueOf(2130837703));
        mSmileyMap.put("叹号", Integer.valueOf(2130837704));
        mSmileyMap.put("天秤座", Integer.valueOf(2130837706));
        mSmileyMap.put("团圆月饼", Integer.valueOf(2130837707));
        mSmileyMap.put("花", Integer.valueOf(2130837708));
        mSmileyMap.put("问号", Integer.valueOf(2130837709));
        mSmileyMap.put("下雨", Integer.valueOf(2130837710));
        mSmileyMap.put("阴天", Integer.valueOf(2130837711));
        mSmileyMap.put("狂笑", Integer.valueOf(2130837712));
        mSmileyMap.put("顶", Integer.valueOf(2130837717));
        mSmileyMap.put("愤怒", Integer.valueOf(2130837718));
        mSmileyMap.put("感冒", Integer.valueOf(2130837719));
        mSmileyMap.put("书呆子", Integer.valueOf(2130837724));
        mSmileyMap.put("困", Integer.valueOf(2130837727));
        mSmileyMap.put("不", Integer.valueOf(2130837731));
        mSmileyMap.put("不活了", Integer.valueOf(2130837733));
        mSmileyMap.put("眨眨眼", Integer.valueOf(2130837734));
        mSmileyMap.put("团", Integer.valueOf(2130837735));
        mSmileyMap.put("多问号", Integer.valueOf(2130837736));
        mSmileyMap.put("圆", Integer.valueOf(2130837737));
        mSmileyMap.put("杂技", Integer.valueOf(2130837738));
        mSmileyMap.put("眨眨眼", Integer.valueOf(2130837739));
        mSmileyMap.put("爱心传递", Integer.valueOf(2130837744));
        mSmileyMap.put("围脖", Integer.valueOf(2130837745));
        mSmileyMap.put("温暖帽子", Integer.valueOf(2130837746));
        mSmileyMap.put("手套", Integer.valueOf(2130837747));
        mSmileyMap.put("雪", Integer.valueOf(2130837748));
        mSmileyMap.put("雪人", Integer.valueOf(2130837749));
        mSmileyMap.put("落叶", Integer.valueOf(2130837750));
        mSmileyMap.put("照相机", Integer.valueOf(2130837751));
        mSmileyMap.put("帅", Integer.valueOf(2130837863));
        mSmileyMap.put("打哈气", Integer.valueOf(2130837720));
        mSmileyMap.put("握手", Integer.valueOf(2130837782));
        mSmileyMap.put("伤心", Integer.valueOf(2130837790));
        mSmileyMap.put("咖啡", Integer.valueOf(2130837843));
        mSmileyMap.put("干杯", Integer.valueOf(2130837806));
        mSmileyMap.put("绿丝带", Integer.valueOf(2130837792));
        mSmileyMap.put("微风", Integer.valueOf(2130837794));
        mSmileyMap.put("月亮", Integer.valueOf(2130837714));
        mSmileyMap.put("做鬼脸", Integer.valueOf(2130837795));
        mSmileyMap.put("萌", Integer.valueOf(2130837941));

        //
        mSmileyMap.put("錢", Integer.valueOf(2130837722));
        mSmileyMap.put("做鬼臉", Integer.valueOf(2130837795));
        mSmileyMap.put("愛心傳遞", Integer.valueOf(2130837744));
        mSmileyMap.put("蠟燭", Integer.valueOf(2130837796));
        mSmileyMap.put("綠絲帶", Integer.valueOf(2130837792));
        mSmileyMap.put("沙塵暴", Integer.valueOf(2130837678));
        mSmileyMap.put("熊貓", Integer.valueOf(2130838028));
        mSmileyMap.put("給力", Integer.valueOf(2130837875));
        mSmileyMap.put("神馬", Integer.valueOf(2130837894));
        mSmileyMap.put("浮雲", Integer.valueOf(2130837752));
        mSmileyMap.put("照相機", Integer.valueOf(2130837751));
        mSmileyMap.put("實習", Integer.valueOf(2130837700));
        mSmileyMap.put("頂", Integer.valueOf(2130837717));
        mSmileyMap.put("傷心", Integer.valueOf(2130837790));
        mSmileyMap.put("鐘", Integer.valueOf(2130837793));
        mSmileyMap.put("豬頭", Integer.valueOf(2130837791));
        mSmileyMap.put("乾杯", Integer.valueOf(2130837806));
        mSmileyMap.put("話筒", Integer.valueOf(2130837859));
        mSmileyMap.put("睡覺", Integer.valueOf(2130837726));
        mSmileyMap.put("吃驚", Integer.valueOf(2130837716));
        mSmileyMap.put("鄙視", Integer.valueOf(2130837765));
        mSmileyMap.put("親親", Integer.valueOf(2130837772));
        mSmileyMap.put("怒罵", Integer.valueOf(2130837774));
        mSmileyMap.put("太開心", Integer.valueOf(2130837775));
        mSmileyMap.put("懶得理你", Integer.valueOf(2130837721));
        mSmileyMap.put("打哈氣", Integer.valueOf(2130837781));
        mSmileyMap.put("贊", Integer.valueOf(2130837648));
        mSmileyMap.put("來", Integer.valueOf(2130837786));
        mSmileyMap.put("圍脖", Integer.valueOf(2130837745));
        mSmileyMap.put("溫暖帽子", Integer.valueOf(2130837746));
        mSmileyMap.put("織", Integer.valueOf(2130837740));
        mSmileyMap.put("圍觀", Integer.valueOf(2130837741));
        mSmileyMap.put("微風", Integer.valueOf(2130837794));
        mSmileyMap.put("落葉", Integer.valueOf(2130837750));
        mSmileyMap.put("差得遠呢", Integer.valueOf(2130837805));
        mSmileyMap.put("帥", Integer.valueOf(2130837863));
        mSmileyMap.put("禮物", Integer.valueOf(2130837753));
        mSmileyMap.put("愛你", Integer.valueOf(2130837705));
        mSmileyMap.put("暈", Integer.valueOf(2130837838));
        mSmileyMap.put("淚", Integer.valueOf(2130837723));
        mSmileyMap.put("饞嘴", Integer.valueOf(2130837756));
        mSmileyMap.put("閉嘴", Integer.valueOf(2130837764));
        mSmileyMap.put("葉子", Integer.valueOf(2130837848));
        mSmileyMap.put("噓", Integer.valueOf(2130837778));
        mSmileyMap.put("怒駡", Integer.valueOf(2130837774));
        mSmileyMap.put("奧特曼", Integer.valueOf(2130837743));
        mSmileyMap.put("自行車", Integer.valueOf(2130837868));
        mSmileyMap.put("可憐", Integer.valueOf(2130837665));*/
    }
}
