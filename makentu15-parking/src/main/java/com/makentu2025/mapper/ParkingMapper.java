package com.makentu2025.mapper;

import com.makentu2025.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface ParkingMapper {
    @Select("select * from makentu2025.parkingspace")
    List<ParkingSpace> getAll();

    @Update("update makentu2025.parkingspace set is_parked = #{isParked}, car_id = #{carId}, update_time = #{updateTime}, scheduled = #{scheduled}, password = #{password} where id = #{id}")
    void update(ParkingSpace space);
}
