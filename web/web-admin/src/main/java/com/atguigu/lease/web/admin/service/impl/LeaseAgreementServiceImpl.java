package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.web.admin.mapper.LeaseAgreementMapper;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.atguigu.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    private final LeaseAgreementMapper leaseAgreementMapper;
    private final ApartmentInfoService apartmentInfoService;
    private final RoomInfoService roomInfoService;
    private final PaymentTypeService paymentTypeService;
    private final LeaseTermService leaseTermService;

    public LeaseAgreementServiceImpl(LeaseAgreementMapper leaseAgreementMapper, ApartmentInfoService apartmentInfoService, RoomInfoService roomInfoService, PaymentTypeService paymentTypeService, LeaseTermService leaseTermService) {
        this.leaseAgreementMapper = leaseAgreementMapper;
        this.apartmentInfoService = apartmentInfoService;
        this.roomInfoService = roomInfoService;
        this.paymentTypeService = paymentTypeService;
        this.leaseTermService = leaseTermService;
    }

    @Override
    public IPage<AgreementVo> pageItemList(IPage<AgreementVo> page, AgreementQueryVo queryVo) {
        return leaseAgreementMapper.pageItemList(page, queryVo);
    }

    @Override
    public AgreementVo getAgreementVoById(Long id) {
        LeaseAgreement leaseAgreement = super.getById(id);
        // 获取公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
        // 获取房间信息
        RoomInfo roomInfo = roomInfoService.getById(leaseAgreement.getRoomId());
        // 获取支付方式
        PaymentType paymentType = paymentTypeService.getById(leaseAgreement.getPaymentTypeId());
        // 获取租期
        LeaseTerm leaseTerm = leaseTermService.getById(leaseAgreement.getLeaseTermId());

        // 组装
        AgreementVo agreementVo = new AgreementVo();
        agreementVo.setApartmentInfo(apartmentInfo);
        agreementVo.setRoomInfo(roomInfo);
        agreementVo.setPaymentType(paymentType);
        agreementVo.setLeaseTerm(leaseTerm);
        BeanUtils.copyProperties(leaseAgreement, agreementVo);
        return agreementVo;
    }
}




