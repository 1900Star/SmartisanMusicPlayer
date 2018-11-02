package com.yibao.music.util;

import com.yibao.music.base.listener.OnSearchFlagListener;
import com.yibao.music.model.MusicBean;
import com.yibao.music.model.greendao.MusicBeanDao;
import com.yibao.music.model.greendao.SearchHistoryBeanDao;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * @ Author: Luoshipeng
 * @ Name:   MusicDaoUtil
 * @ Email:  strangermy98@gmail.com
 * @ Time:   2018/9/14/ 16:38
 * @ Des:    搜索相关操作
 */
public class MusicDaoUtil {
    /**
     * 获取音乐搜索结果
     *
     * @param musicBeanDao    查询音乐的Dao
     * @param queryConditions 查询的关键字
     * @return 查询结果以集合的类型返回
     */
    public static Observable<List<MusicBean>> getSearchResult(OnSearchFlagListener listener, MusicBeanDao musicBeanDao, String queryConditions) {
        LogUtil.d(" ====  时时查询关键字  " + queryConditions);
        return Observable.create((ObservableOnSubscribe<List<MusicBean>>) emitter -> {
            List<MusicBean> artistList = musicBeanDao.queryBuilder().where(MusicBeanDao.Properties.Artist.eq(queryConditions)).build().list();
            if (artistList != null && artistList.size() > 0) {
                listener.setSearchFlag(1);
                insertSearchBean(emitter, artistList);
            } else {
                List<MusicBean> albumList = musicBeanDao.queryBuilder().where(MusicBeanDao.Properties.Album.eq(queryConditions)).build().list();
                if (albumList != null && albumList.size() > 0) {
                    listener.setSearchFlag(2);
                    insertSearchBean(emitter, albumList);
                } else {
                    // 根据歌名精确搜索
                    List<MusicBean> songList = musicBeanDao.queryBuilder().where(MusicBeanDao.Properties.Title.eq(queryConditions)).build().list();
                    if (songList != null && songList.size() > 0) {
                        listener.setSearchFlag(3);
                        insertSearchBean(emitter, songList);
                    } else {
                        // 模糊匹配搜索
                        List<MusicBean> searchSongList = new ArrayList<>();
                        List<MusicBean> beanList = musicBeanDao.queryBuilder().build().list();
                        for (MusicBean musicBean : beanList) {
                            if (musicBean.getTitle().startsWith(queryConditions)) {
                                searchSongList.add(musicBean);
                            }
                        }
                        if (searchSongList.size() == 0) {
                            emitter.onError(new FileNotFoundException());
                        } else {
                            listener.setSearchFlag(3);
                            insertSearchBean(emitter, searchSongList);
                        }
                    }


                }
            }

        }).subscribeOn(Schedulers.io());


    }

    /**
     * 将一个搜索结果保存到本地，同时做了重复保存的判断。
     */
    private static void insertSearchBean(ObservableEmitter<List<MusicBean>> emitter, List<MusicBean> beanList) {
        emitter.onNext(beanList);
        emitter.onComplete();
    }
}