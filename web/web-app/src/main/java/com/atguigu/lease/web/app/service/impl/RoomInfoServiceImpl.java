package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    private final RoomInfoMapper roomInfoMapper;
    private final ApartmentInfoService apartmentInfoService;
    private final AttrValueMapper attrValueMapper;
    private final FacilityInfoMapper facilityInfoMapper;
    private final LabelInfoMapper labelInfoMapper;
    private final PaymentTypeMapper paymentTypeMapper;
    private final FeeValueMapper feeValueMapper;
    private final LeaseTermMapper leaseTermMapper;
    private final GraphInfoMapper graphInfoMapper;
    private final BrowsingHistoryService browsingHistoryService;
    private final RedisTemplate<String, Object> redisTemplate;

    public RoomInfoServiceImpl(AttrValueMapper attrValueMapper, RoomInfoMapper roomInfoMapper, ApartmentInfoService apartmentInfoService, FacilityInfoMapper facilityInfoMapper, LabelInfoMapper labelInfoMapper, PaymentTypeMapper paymentTypeMapper, FeeValueMapper feeValueMapper, LeaseTermMapper leaseTermMapper, GraphInfoMapper graphInfoMapper, BrowsingHistoryService browsingHistoryService, RedisTemplate<String, Object> redisTemplate) {
        this.attrValueMapper = attrValueMapper;
        this.roomInfoMapper = roomInfoMapper;
        this.apartmentInfoService = apartmentInfoService;
        this.facilityInfoMapper = facilityInfoMapper;
        this.labelInfoMapper = labelInfoMapper;
        this.paymentTypeMapper = paymentTypeMapper;
        this.feeValueMapper = feeValueMapper;
        this.leaseTermMapper = leaseTermMapper;
        this.graphInfoMapper = graphInfoMapper;
        this.browsingHistoryService = browsingHistoryService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public IPage<RoomItemVo> pageItem(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        return  roomInfoMapper.pageItem(page, queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if (roomInfo == null) {
            return null;
        }
        // 更新浏览记录
        browsingHistoryService.saveHistory(LoginUserHolder.getLoginUser().getUserId(), id);

        // 获取缓存
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = (RoomDetailVo)redisTemplate.opsForValue().get(key);
        // 缓存命中, 直接返回
        if (roomDetailVo != null) {
            return roomDetailVo;
        }
        // 获取所属公寓信息
        ApartmentItemVo itemById = apartmentInfoService.getItemById(roomInfo.getApartmentId());
        // 获取图片列表
        List<GraphVo> graphVos = graphInfoMapper.selectVoByIdAndItemType(id, ItemType.ROOM);
        // 获取属性信息列表
        List<AttrValueVo> attrValueVoList = attrValueMapper.selectVoByRoomId(id);
        // 获取配套信息列表
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListRoomId(id);
        // 获取标签信息列表
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListRoomId(id);
        // 获取支付方式列表
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListRoomId(id);
        // 获取杂费列表
        List<FeeValueVo> feeValueVoList = feeValueMapper.selectVoByRoomId(id);
        // 获取租期列表
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);
        // 组装
        roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);
        roomDetailVo.setApartmentItemVo(itemById);
        roomDetailVo.setGraphVoList(graphVos);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setFeeValueVoList(feeValueVoList);
        roomDetailVo.setLeaseTermList(leaseTermList);

        // 写入缓存
        redisTemplate.opsForValue().set(key, roomDetailVo);
        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(IPage<RoomItemVo> page, Long id) {
        return roomInfoMapper.pageItemByApartmentId(page, id);
    }

}




