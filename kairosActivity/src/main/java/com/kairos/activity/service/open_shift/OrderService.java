package com.kairos.activity.service.open_shift;

import com.kairos.activity.custom_exception.DataNotFoundByIdException;
import com.kairos.activity.persistence.model.open_shift.Order;
import com.kairos.activity.persistence.repository.open_shift.OrderMongoRepository;
import com.kairos.activity.service.MongoBaseService;
import com.kairos.activity.service.phase.PhaseService;
import com.kairos.activity.service.priority_group.PriorityGroupService;
import com.kairos.activity.util.ObjectMapperUtils;
import com.kairos.response.dto.web.open_shift.OpenShiftResponseDTO;
import com.kairos.response.dto.web.open_shift.OrderOpenshiftResponseDTO;
import com.kairos.response.dto.web.open_shift.OrderResponseDTO;
import com.kairos.response.dto.web.open_shift.priority_group.PriorityGroupDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService extends MongoBaseService {

    private static final Logger logger = LoggerFactory.getLogger(PhaseService.class);
    @Inject
    private OrderMongoRepository orderMongoRepository;
    @Inject
    private PriorityGroupService priorityGroupService;
    @Inject
    private OpenShiftService openShiftService;



   public OrderOpenshiftResponseDTO createOrder(Long unitId,OrderOpenshiftResponseDTO orderOpenshiftResponseDTO) {

    Order order = new Order();
    OrderResponseDTO orderResponseDTO = orderOpenshiftResponseDTO.getOrder();
    List<OpenShiftResponseDTO> openShiftResponseDTOs = orderOpenshiftResponseDTO.getOpenshifts();
    ObjectMapperUtils.copyProperties(orderResponseDTO,order);
    save(order);
    orderResponseDTO.setId(order.getId());
    //priorityGroupService.copyPriorityGroupsForOrder(orderResponseDTO.getUnitId(),order.getId());
       for(OpenShiftResponseDTO openShiftResponseDTO : openShiftResponseDTOs) {
           openShiftResponseDTO.setActivityId(order.getActivityId());
       }
       openShiftService.createOpenShiftFromOrder(openShiftResponseDTOs, order.getId());
       List<PriorityGroupDTO> priorityGroupDTOS=priorityGroupService.createPriorityGroups(order.getId(),orderOpenshiftResponseDTO.getPriorityGroups());
       orderOpenshiftResponseDTO.setPriorityGroups(priorityGroupDTOS);
       BigInteger id = null;
       for(PriorityGroupDTO priorityGroupDTO:priorityGroupDTOS) {
           if(priorityGroupDTO.getName().toString().equals("PRIORITY_GROUP1")) {
               id = priorityGroupDTO.getId();
               break;
           }
       }
       priorityGroupService.notifyStaffByPriorityGroup(id);

       return orderOpenshiftResponseDTO;
       }

    public OrderOpenshiftResponseDTO updateOrder(OrderOpenshiftResponseDTO orderOpenShiftResponseDTO,BigInteger orderId) {

       OrderResponseDTO orderResponseDTO = orderOpenShiftResponseDTO.getOrder();
       List<OpenShiftResponseDTO> openShiftResponseDTOS = orderOpenShiftResponseDTO.getOpenshifts();
        Order order = orderMongoRepository.findOrderByIdAndEnabled(orderId);
        if(!Optional.ofNullable(order).isPresent()) {
            throw new DataNotFoundByIdException("Order doesn not exist by id"+ orderId);
        }
        ObjectMapperUtils.copyProperties(orderResponseDTO,order);
        save(order);
        orderResponseDTO.setId(order.getId());
        openShiftResponseDTOS = openShiftService.updateOpenShift(openShiftResponseDTOS,orderId);
        List<PriorityGroupDTO> priorityGroupDTOs=orderOpenShiftResponseDTO.getPriorityGroups();
        priorityGroupService.updatePriorityGroupsForOrder(priorityGroupDTOs);
        orderOpenShiftResponseDTO.setOrder(orderResponseDTO);
        orderOpenShiftResponseDTO.setOpenshifts(openShiftResponseDTOS);
        orderOpenShiftResponseDTO.setPriorityGroups(priorityGroupDTOs);
        return orderOpenShiftResponseDTO;
    }

    public void deleteOrder(BigInteger orderId) {

        Order order = orderMongoRepository.findOrderByIdAndEnabled(orderId);
        if(!Optional.ofNullable(order).isPresent()) {
            throw new DataNotFoundByIdException("Order doesn not exist by id"+ orderId);
        }
        order.setDeleted(true);
        save(order);

    }


    public List<Order> getOrdersByUnitId(Long unitId) {
       List<Order> orders = orderMongoRepository.findOrdersByUnitId(unitId);
        return orders;
    }

   public OrderOpenshiftResponseDTO getPriorityGroupAndOpenShiftsByOrderId(Long unitId,BigInteger orderId){
       List<OpenShiftResponseDTO> openShiftResponseDTOS=openShiftService.getOpenShiftsByUnitIdAndOrderId(unitId,orderId);
       List<PriorityGroupDTO> priorityGroupDTOS=priorityGroupService.getPriorityGroupsByOrderId(unitId,orderId);
        return  new OrderOpenshiftResponseDTO(openShiftResponseDTOS,priorityGroupDTOS);
    }
}
