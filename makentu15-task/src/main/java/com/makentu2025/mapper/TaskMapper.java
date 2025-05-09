package com.makentu2025.mapper;

import com.makentu2025.entity.ParkingSpace;
import com.makentu2025.entity.Request;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Select("select * from makentu2025.parkingspace")
    List<ParkingSpace> showAllParkingSpaces();

    @Insert("insert into makentu2025.request(`option`, id, serial, update_time) values (#{option}, #{id}, #{serial}, #{updateTime})")
    void insertTask(Request request);
    @Delete("delete from makentu2025.request where serial = #{serial}")
    void clearTask(Integer serial);
    @Select("select * from makentu2025.request")
    List<Request> showAllTasks();


    @Select("select id from makentu2025.request where serial = #{serial}")
    Integer searchBySerial(Integer serial);
    @Update("update makentu2025.parkingspace set scheduled = 0 where id = #{id}")
    void unSchedule(Integer id);
}
