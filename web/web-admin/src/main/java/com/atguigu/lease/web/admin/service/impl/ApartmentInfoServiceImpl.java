package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    private final ApartmentInfoMapper apartmentInfoMapper;

    private final GraphInfoService graphInfoService;
    private final ApartmentFacilityService apartmentFacilityService;
    private final ApartmentLabelService apartmentLabelService;
    private final ApartmentFeeValueService apartmentFeeValueService;
    private final GraphInfoMapper graphInfoMapper;
    private final LabelInfoMapper labelInfoMapper;
    private final FacilityInfoMapper facilityInfoMapper;
    private final FeeValueMapper feeValueMapper;
    private final RoomInfoService roomInfoService;
    private final ProvinceInfoMapper provinceInfoMapper;
    private final CityInfoMapper cityInfoMapper;
    private final DistrictInfoMapper districtInfoMapper;

    public ApartmentInfoServiceImpl(GraphInfoService graphInfoService,
                                    ApartmentFacilityService apartmentFacilityService,
                                    ApartmentLabelService apartmentLabelService,
                                    ApartmentFeeValueService apartmentFeeValueService,
                                    ApartmentInfoMapper apartmentInfoMapper,
                                    GraphInfoMapper graphInfoMapper, LabelInfoMapper labelInfoMapper, FacilityInfoMapper facilityInfoMapper, FeeValueMapper feeValueMapper, RoomInfoService roomInfoService, ProvinceInfoMapper provinceInfoMapper, CityInfoMapper cityInfoMapper, DistrictInfoMapper districtInfoMapper) {
        this.graphInfoService = graphInfoService;
        this.apartmentFacilityService = apartmentFacilityService;
        this.apartmentLabelService = apartmentLabelService;
        this.apartmentFeeValueService = apartmentFeeValueService;
        this.apartmentInfoMapper = apartmentInfoMapper;
        this.graphInfoMapper = graphInfoMapper;
        this.labelInfoMapper = labelInfoMapper;
        this.facilityInfoMapper = facilityInfoMapper;
        this.feeValueMapper = feeValueMapper;
        this.roomInfoService = roomInfoService;
        this.provinceInfoMapper = provinceInfoMapper;
        this.cityInfoMapper = cityInfoMapper;
        this.districtInfoMapper = districtInfoMapper;
    }

    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        // 图片, 配套, 标签, 杂费
        boolean isUpdate = apartmentSubmitVo.getId() != null;
        // 更改地区
        String provinceName = provinceInfoMapper.selectById(apartmentSubmitVo.getProvinceId()).getName();
        String cityName = cityInfoMapper.selectById(apartmentSubmitVo.getCityId()).getName();
        String districtName = districtInfoMapper.selectById(apartmentSubmitVo.getDistrictId()).getName();
        apartmentSubmitVo.setProvinceName(provinceName);
        apartmentSubmitVo.setCityName(cityName);
        apartmentSubmitVo.setDistrictName(districtName);
        super.saveOrUpdate(apartmentSubmitVo);
        if(isUpdate) {
            // 删除图片信息
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId())
                    .eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoService.remove(graphQueryWrapper);
            // 删除配套信息
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, apartmentSubmitVo.getId());
            apartmentFacilityService.remove(apartmentFacilityQueryWrapper);
            // 删除标签信息
            LambdaQueryWrapper<ApartmentLabel> labelInfoQueryWrapper = new LambdaQueryWrapper<>();
            labelInfoQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
            apartmentLabelService.remove(labelInfoQueryWrapper);
            // 删除杂费信息
            LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(apartmentFeeValueQueryWrapper);
        }

        // 插入图片信息
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)) {
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphVo.getName());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }

        // 插入配套信息
        List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIdList)) {
            ArrayList<ApartmentFacility> apartmentFacilityList = new ArrayList<>();
            for (Long facilityId : facilityInfoIdList) {
                ApartmentFacility apartmentFacility = ApartmentFacility.builder()
                        .apartmentId(apartmentSubmitVo.getId())
                        .facilityId(facilityId)
                        .build();
                apartmentFacilityList.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(apartmentFacilityList);
        }

        // 插入标签信息
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if (!CollectionUtils.isEmpty(labelIds)) {
            ArrayList<ApartmentLabel> apartmentLabelList = new ArrayList<>();
            for (Long labelId : labelIds) {
                ApartmentLabel apartmentLabel = ApartmentLabel.builder()
                        .apartmentId(apartmentSubmitVo.getId())
                        .labelId(labelId)
                        .build();
                apartmentLabelList.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(apartmentLabelList);
        }

        // 插入杂费信息
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)) {
            ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
            for (Long feeValueId : feeValueIds) {
                ApartmentFeeValue apartmentFeeValue = ApartmentFeeValue.builder()
                        .apartmentId(apartmentSubmitVo.getId())
                        .feeValueId(feeValueId)
                        .build();
                apartmentFeeValueList.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(apartmentFeeValueList);
        }


    }

    @Override
    public IPage<ApartmentItemVo> pageApartmentItemVoByQuery(IPage<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageApartmentItemVoByQuery(page, queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        ApartmentInfo apartmentInfo = this.getById(id);
        if(apartmentInfo==null) return null;
        // 查询图片列表(VO)
        List<GraphVo> graphVos = graphInfoMapper.selectVoByIdAndItemType(id, ItemType.APARTMENT);
        // 查询标签列表
        List<LabelInfo> labelInfos = labelInfoMapper.selectListByApartmentId(id);
        // 查询配套列表
        List<FacilityInfo> facilityInfos = facilityInfoMapper.selectListByApartmentId(id);
        // 查询杂费列表(VO)
        List<FeeValueVo> feeValueVos = feeValueMapper.selectListByApartmentId(id);

        // 组装
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVos);
        apartmentDetailVo.setLabelInfoList(labelInfos);
        apartmentDetailVo.setFacilityInfoList(facilityInfos);
        apartmentDetailVo.setFeeValueVoList(feeValueVos);
        String provinceName = provinceInfoMapper.selectById(apartmentInfo.getProvinceId()).getName();
        String cityName = cityInfoMapper.selectById(apartmentInfo.getCityId()).getName();
        String districtName = districtInfoMapper.selectById(apartmentInfo.getDistrictId()).getName();
        apartmentDetailVo.setProvinceName(provinceName);
        apartmentDetailVo.setCityName(cityName);
        apartmentDetailVo.setDistrictName(districtName);
        return apartmentDetailVo;
    }

    @Override
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomInfo::getApartmentId, id);
        long count = roomInfoService.count(queryWrapper);
        if(count > 0) {
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }

        super.removeById(id);
        // 删除图片信息
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphInfoService.remove(graphQueryWrapper);
        // 删除配套信息
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        apartmentFacilityService.remove(apartmentFacilityQueryWrapper);
        // 删除标签信息
        LambdaQueryWrapper<ApartmentLabel> labelInfoQueryWrapper = new LambdaQueryWrapper<>();
        labelInfoQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelService.remove(labelInfoQueryWrapper);
        // 删除杂费信息
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueService.remove(apartmentFeeValueQueryWrapper);

        /*// 删除房间
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, id);
        roomInfoService.remove(roomInfoQueryWrapper);*/
    }
}




