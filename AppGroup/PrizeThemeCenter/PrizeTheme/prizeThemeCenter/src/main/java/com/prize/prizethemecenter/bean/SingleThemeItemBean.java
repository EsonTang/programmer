package com.prize.prizethemecenter.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/12.
 */
public class SingleThemeItemBean implements Parcelable {


    /**
     * id : 31
     * name : 夏天的风
     * package_uri : http://ad-client.oss-cn-beijing.aliyuncs.com/kuang/2016-07-30/firstProj2.apk
     * size : 525.27 KB
     * title : 凉爽
     * ad_pictrue : http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1473150827.png
     * screenshot : ["http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1473149900.png","http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1473149901.png","http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1473149902.png","http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1473149903.png"]
     * intro : 反对感到孤独是根深蒂固大法官 梵蒂冈多少地方敢死队风格地方斯蒂芬多少的鬼地方感到十分
     * price : 3.00
     * download_count : 0
     * updateTime : 2016-09-08 15:44:25
     * comments : {"total":"9","Item":[{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的1"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的2"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的3"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的4"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的5"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的6"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的7"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的8"}]}
     * types : [{"id":"2","name":"手绘"},{"id":"3","name":"动漫"}]
     */

    private List<ItemsBean> items;

    public SingleThemeItemBean(Parcel in) {
        items = in.createTypedArrayList(ItemsBean.CREATOR);
    }

    public static final Creator<SingleThemeItemBean> CREATOR = new Creator<SingleThemeItemBean>() {
        @Override
        public SingleThemeItemBean createFromParcel(Parcel in) {
            return new SingleThemeItemBean(in);
        }

        @Override
        public SingleThemeItemBean[] newArray(int size) {
            return new SingleThemeItemBean[size];
        }
    };

    public SingleThemeItemBean() {

    }

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(items);
    }

    public static class ItemsBean implements Parcelable {
        public String id;
        public String name;
        public String package_uri;
        public String size;
        public String title;
        public String ad_pictrue;
        public String intro;
        public String price;
        public String download_count;
        public String updateTime;

        public String wallpaper_type;
        public String wallpaper_pic;
        public String download_url;

        public String is_pay;
        public String is_buy;
        public String status;
        public String md5;
        public int type;

        public ItemsBean() {
        }

        protected ItemsBean(Parcel in) {
            id = in.readString();
            name = in.readString();
            package_uri = in.readString();
            size = in.readString();
            title = in.readString();
            ad_pictrue = in.readString();
            intro = in.readString();
            price = in.readString();
            download_count = in.readString();
            updateTime = in.readString();
            wallpaper_type = in.readString();
            wallpaper_pic = in.readString();
            download_url = in.readString();
            is_pay = in.readString();
            is_buy = in.readString();
            status = in.readString();
            md5 = in.readString();
            type = in.readInt();
            thumbnail = in.readString();
            comments = in.readParcelable(CommentsBean.class.getClassLoader());
//            screenshot = in.createStringArrayList();
        }

        public static final Creator<ItemsBean> CREATOR = new Creator<ItemsBean>() {
            @Override
            public ItemsBean createFromParcel(Parcel in) {
                return new ItemsBean(in);
            }

            @Override
            public ItemsBean[] newArray(int size) {
                return new ItemsBean[size];
            }
        };

        public String getIs_buy() {
            return is_buy;
        }

        public void setIs_buy(String is_buy) {
            this.is_buy = is_buy;
        }

        public String getStatus() {
            return status;
        }

        public String getMd5() {
            return md5;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }
        /**
         * 缩略图
         */
        private String thumbnail;
        /**
         * total : 9
         * Item : [{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的1"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的2"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的3"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的4"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的5"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的6"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的7"},{"user_id":"1","date":"2016-07-20","content":"我是来抢沙发的8"}]
         */

        private CommentsBean comments;
        private List<String> screenshot;
        /**
         * id : 2
         * name : 手绘
         */

        private List<TypesBean> types;

        public String getIs_pay() {
            return is_pay;
        }


        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        //        public void setIs_pay(String is_pay){
//            this.is_pay = is_pay;
//        }
        public String getWallpaper_type() {
            return wallpaper_type;
        }

        public String getWallpaper_pic() {
            return wallpaper_pic;
        }

        public String getDownload_url() {
            return download_url;
        }

        public void setDownload_url(String download_url) {
            this.download_url = download_url;
        }
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPackage_uri() {
            return package_uri;
        }

        public void setPackage_uri(String package_uri) {
            this.package_uri = package_uri;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAd_pictrue() {
            return ad_pictrue;
        }

        public void setAd_pictrue(String ad_pictrue) {
            this.ad_pictrue = ad_pictrue;
        }

        public String getIntro() {
            return intro;
        }

        public void setIntro(String intro) {
            this.intro = intro;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getDownload_count() {
            return download_count;
        }

        public void setDownload_count(String download_count) {
            this.download_count = download_count;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public CommentsBean getComments() {
            return comments;
        }

        public void setComments(CommentsBean comments) {
            this.comments = comments;
        }

        public List<String> getScreenshot() {
            return screenshot;
        }

        public void setScreenshot(List<String> screenshot) {
            this.screenshot = screenshot;
        }

        public List<TypesBean> getTypes() {
            return types;
        }

        public void setTypes(List<TypesBean> types) {
            this.types = types;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(package_uri);
            dest.writeString(size);
            dest.writeString(title);
            dest.writeString(ad_pictrue);
            dest.writeString(intro);
            dest.writeString(price);
            dest.writeString(download_count);
            dest.writeString(updateTime);
            dest.writeString(wallpaper_type);
            dest.writeString(wallpaper_pic);
            dest.writeString(download_url);
            dest.writeString(is_pay);
            dest.writeString(is_buy);
            dest.writeString(status);
            dest.writeString(md5);
            dest.writeInt(type);
            dest.writeString(thumbnail);
            dest.writeParcelable(comments, flags);
//            dest.writeStringList(screenshot);
        }

        public static class CommentsBean implements Parcelable {
            private String total;
            /**
             * user_id : 1
             * date : 2016-07-20
             * content : 我是来抢沙发的
             */

            private List<ItemBean> Item;

            public String getTotal() {
                return total;
            }

            public void setTotal(String total) {
                this.total = total;
            }

            public List<ItemBean> getItem() {
                return Item;
            }

            public void setItem(List<ItemBean> Item) {
                this.Item = Item;
            }

            public static class ItemBean implements Parcelable {
                private String user_id;
                private String date;
                private String content;
                private String icon;

                public String getIcon() {
                    return icon;
                }

                public void setIcon(String icon) {
                    this.icon = icon;
                }

                public String getUser_id() {
                    return user_id;
                }

                public void setUser_id(String user_id) {
                    this.user_id = user_id;
                }

                public String getDate() {
                    return date;
                }

                public void setDate(String date) {
                    this.date = date;
                }

                public String getContent() {
                    return content;
                }

                public void setContent(String content) {
                    this.content = content;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeString(this.user_id);
                    dest.writeString(this.date);
                    dest.writeString(this.content);
                    dest.writeString(this.icon);
                }

                public ItemBean() {
                }

                protected ItemBean(Parcel in) {
                    this.user_id = in.readString();
                    this.date = in.readString();
                    this.content = in.readString();
                    this.icon = in.readString();
                }

                public static final Creator<ItemBean> CREATOR = new Creator<ItemBean>() {
                    @Override
                    public ItemBean createFromParcel(Parcel source) {
                        return new ItemBean(source);
                    }

                    @Override
                    public ItemBean[] newArray(int size) {
                        return new ItemBean[size];
                    }
                };
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.total);
                dest.writeList(this.Item);
            }

            public CommentsBean() {
            }

            protected CommentsBean(Parcel in) {
                this.total = in.readString();
                this.Item = new ArrayList<ItemBean>();
                in.readList(this.Item, ItemBean.class.getClassLoader());
            }

            public static final Parcelable.Creator<CommentsBean> CREATOR = new Parcelable.Creator<CommentsBean>() {
                @Override
                public CommentsBean createFromParcel(Parcel source) {
                    return new CommentsBean(source);
                }

                @Override
                public CommentsBean[] newArray(int size) {
                    return new CommentsBean[size];
                }
            };
        }

        public static class TypesBean {
            private String id;
            private String name;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
