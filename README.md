This is a sina weibo thirdparty app.
it required api level >=14.

主工程:APlane

APlane 依赖:apiLibrary核心库,api实现等.

APlane 依赖: bulletnoidStaggeredGridViewLibrary,瀑布流的实现,只用于测试的.

APlane 依赖:ImageFetcherLibrary图片下载,存储(磁盘控制大小的缓存) ,基于apollo改造的.

AUILlibrary著名的图片处理库,但不再使用,

APlane 依赖: PhotoViewLibrary大图片查看库,

APlane 依赖: pulltorefreshlibrary下拉,上拉的库, bulletnoidStaggeredGridViewLibrary这个也依赖它 

APlane 依赖:roundedimageview圆形头像处理的库.

SlidingMenuLibrary侧边栏,由于性能不好,会导致右侧部分layout所以已经不使用了.替换为support包中的实现.

所有的lib是在 APlane/libs中的,所以需要在使用eclipse中,单独指定,而使用idea则建立一个library,同时供所有的工程使用即可.
 
