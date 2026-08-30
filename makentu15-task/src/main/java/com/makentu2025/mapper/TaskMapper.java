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

    @Select("select * from makentu2025.request where serial = #{serial}")
    Request findBySerial(Integer serial);

    /** 停車任務做完：車子這時才真的在格子裡。 */
    @Update("update makentu2025.parkingspace set is_parked = 1, scheduled = 0, update_time = now() where id = #{id}")
    void completePark(Integer id);

    /** 取車任務做完：車子離開，憑證跟著失效，格子可以再被配出去。 */
    @Update("update makentu2025.parkingspace set is_parked = 0, scheduled = 0, car_id = null, password = null, update_time = now() where id = #{id}")
    void completeTake(Integer id);
}
