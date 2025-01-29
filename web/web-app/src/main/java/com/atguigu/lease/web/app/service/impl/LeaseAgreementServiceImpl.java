package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.GraphInfo;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.LeaseAgreementService;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Override
    public List<AgreementItemVo> listItem() {
        return leaseAgreementMapper.selectAgreementItemVoByPhone(LoginUserHolder.getLoginUser().getUsername());
    }

    @Override
    public AgreementDetailVo getDetailById(Long id) {
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if (leaseAgreement == null) {
            return null;
        }

        // 获取公寓名称
        String apartmentName = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId()).getName();
        // 获取公寓图片列表
        List<GraphVo> apartmentGraphs = graphInfoMapper.selectVoByIdAndItemType(leaseAgreement.getApartmentId(), ItemType.APARTMENT);
        // 获取房间号
        String roomNumber = roomInfoMapper.selectById(leaseAgreement.getRoomId()).getRoomNumber();
        // 获取房间图片列表
        List<GraphVo> roomGraphs = graphInfoMapper.selectVoByIdAndItemType(leaseAgreement.getRoomId(), ItemType.ROOM);
        // 获取支付方式名称
        String paymentTypeName = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId()).getName();
        // 获取租期月数和单位
        Integer leaseTermMonthCount = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId()).getMonthCount();
        String leaseTermUnit = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId()).getUnit();

        // 组装
        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);
        agreementDetailVo.setApartmentName(apartmentName);
        agreementDetailVo.setApartmentGraphVoList(apartmentGraphs);
        agreementDetailVo.setRoomNumber(roomNumber);
        agreementDetailVo.setRoomGraphVoList(roomGraphs);
        agreementDetailVo.setPaymentTypeName(paymentTypeName);
        agreementDetailVo.setLeaseTermMonthCount(leaseTermMonthCount);
        agreementDetailVo.setLeaseTermUnit(leaseTermUnit);
        return agreementDetailVo;
    }
}




