package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if (apartmentInfo == null) return null;

        // 获取图片列表
        List<GraphVo> graphVos = graphInfoMapper.selectVoByIdAndItemType(id, ItemType.APARTMENT);
        // 获取标签列表
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);
        // 获取配套列表
        List<FacilityInfo> facilityInfos = facilityInfoMapper.selectListByApartmentId(id);
        // 获取租金最小值
        BigDecimal minRent = roomInfoMapper.getMinRentByApartmentId(id);

        // 组装数据
        ApartmentDetailVo detail = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo, detail);
        detail.setGraphVoList(graphVos);
        detail.setLabelInfoList(labelInfoList);
        detail.setFacilityInfoList(facilityInfos);
        detail.setMinRent(minRent);
        return detail;
    }

    @Override
    public ApartmentItemVo getItemById(Long apartmentId) {
        ApartmentDetailVo detailById = getDetailById(apartmentId);
        if (detailById == null) return null;
        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(detailById, apartmentItemVo);
        return apartmentItemVo;
    }
}




