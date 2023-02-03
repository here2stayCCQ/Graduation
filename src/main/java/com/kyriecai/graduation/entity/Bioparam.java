package com.kyriecai.graduation.entity;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 
 * </p>
 *
 * @author kyriecai
 * @since 2022-05-06
 */

@Data
@TableName("sys_bioparam")
@ApiModel(value = "Bioparam对象", description = "")
public class Bioparam implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      @ApiModelProperty("检验结果变量")
      @Alias("检验结果变量")
      private String variable;

      @ApiModelProperty("指标含义")
      @Alias("指标含义")
      private String indexMeaning;

      @ApiModelProperty("auc")
      @Alias("AUC")
      private Double auc;

      @ApiModelProperty("状态")
      @Alias("状态")
      private String status;

      @ApiModelProperty("标准错误a")
      @Alias("标准错误a")
      private Double standardError;

      @ApiModelProperty("渐进显著性b")
      @Alias("渐进显著性b")
      private Double asymptoticSignificance;

      @ApiModelProperty("渐进95%置信区间下限")
      @Alias("渐进95%置信区间下限")
      private Double lowerLimit;

      @ApiModelProperty("渐进95%置信区间上限")
      @Alias("渐进95%置信区间上限")
      private Double higherLimit;

      private Integer uploaderId;
}
