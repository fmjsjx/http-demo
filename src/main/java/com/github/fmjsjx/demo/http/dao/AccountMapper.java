package com.github.fmjsjx.demo.http.dao;

import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.github.fmjsjx.demo.http.entity.Account;

public interface AccountMapper {

    @Insert("""
            INSERT INTO `tb_account`
            (`gid`,`type`,`state`,`product_id`,`channel`,`channel_id`,`partner`,`guest_id`,`openid`,`unionid`,
             `apple_id`,`ip`,`client_version`,`device_id`,`slot`,`device_info`,`os_info`,`imei`,`oaid`,`ext_s1`)
            VALUES
            (#{gid},#{type},#{state},#{productId},#{channel},#{channelId},#{partner},#{guestId},#{openid},#{unionid},
             #{appleId},#{ip},#{clientVersion},#{deviceId},#{slot},#{deviceInfo},#{osInfo},#{imei},#{oaid},#{extS1})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "uid", keyColumn = "uid")
    int insertOne(Account account);

    @Select("""
            SELECT
              `uid`,`gid`,`type`,`state`,`product_id`,`channel`,`channel_id`,`partner`,`guest_id`,`openid`,
              `unionid`,`apple_id`,`ip`,`client_version`,`device_id`,`slot`,`device_info`,`os_info`,`imei`,`oaid`,
              `ext_s1`,`create_time`,`update_time`
            FROM
              `tb_account`
            WHERE
              `uid` = #{uid}
            """)
    Optional<Account> selectOne(int uid);

    @Select("""
            SELECT
              `uid`,`gid`,`type`,`state`,`product_id`,`channel`,`channel_id`,`partner`,`guest_id`,`openid`,
              `unionid`,`apple_id`,`ip`,`client_version`,`device_id`,`slot`,`device_info`,`os_info`,`imei`,`oaid`,
              `ext_s1`,`create_time`,`update_time`
            FROM
              `tb_account`
            WHERE
              `guest_id` = #{guestId}
            LIMIT 1
            """)
    Optional<Account> selectOneByGuestId(String guestId);

    @Select("""
            SELECT
              `uid`,`gid`,`type`,`state`,`product_id`,`channel`,`channel_id`,`partner`,`guest_id`,`openid`,
              `unionid`,`apple_id`,`ip`,`client_version`,`device_id`,`slot`,`device_info`,`os_info`,`imei`,`oaid`,
              `ext_s1`,`create_time`,`update_time`
            FROM
              `tb_account`
            WHERE
              `partner` = #{partner} AND `openid` = #{openid}
            LIMIT 1
            """)
    Optional<Account> selectOneByOpenid(int partner, String openid);

    @Select("""
            SELECT
              `uid`,`gid`,`type`,`state`,`product_id`,`channel`,`channel_id`,`partner`,`guest_id`,`openid`,
              `unionid`,`apple_id`,`ip`,`client_version`,`device_id`,`slot`,`device_info`,`os_info`,`imei`,`oaid`,
              `ext_s1`,`create_time`,`update_time`
            FROM
              `tb_account`
            WHERE
              `apple_id` = #{appleId}
            LIMIT 1
            """)
    Optional<Account> selectOneByAppleId(String appleId);

}
