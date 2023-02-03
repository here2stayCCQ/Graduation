package com.kyriecai.graduation.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author kyriecai
 * @since 2022-05-05
 */
@Getter
@Setter
  @TableName("sys_normcorneal")
@ApiModel(value = "Normcorneal对象", description = "")
public class Normcorneal implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("id")
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("病例号")
      @Alias("病例号")
      private String patientNum;

      @ApiModelProperty("姓名")
      @Alias("姓名")
      private String name;

      @ApiModelProperty("性别")
      @Alias("性别")
      private String gender;

      @ApiModelProperty("眼别")
      @Alias("眼别")
      private String eye;

      @ApiModelProperty("等效球镜")
      @Alias("等效球镜")
      private Double esm;

      @ApiModelProperty("球镜")
      @Alias("球镜")
      private Double sphericalMirror;

      @ApiModelProperty("柱镜")
      @Alias("柱镜")
      private Double cylinder;

      @ApiModelProperty("轴向")
      @Alias("轴向")
      private Integer axial;

      @ApiModelProperty("角膜厚度")
      @Alias("角膜厚度")
      private Integer cornealThickness;

      @ApiModelProperty("IOP")
      @Alias("IOP")
      private Double iop;

      @ApiModelProperty("bIOP")
      @Alias("bIOP")
      private Double biop;

      private Integer uploaderId;
}
