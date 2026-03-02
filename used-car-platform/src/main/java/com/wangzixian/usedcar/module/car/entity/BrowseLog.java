package com.wangzixian.usedcar.module.car.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity // 👈 补上这个
@TableName("sys_browse_log")
@Table(name="sys_browse_log") // 👈 补上这个
public class BrowseLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer carId;
    private String brand;
    private Double price;
    private LocalDateTime browseTime;
}