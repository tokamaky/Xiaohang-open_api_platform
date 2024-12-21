package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.UserInterfaceInfo;
import generator.service.UserInterfaceInfoService;
import generator.mapper.UserInterfaceInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author 14711
* @description 针对表【user_interface_info(User-API relationship)】的数据库操作Service实现
* @createDate 2024-12-20 19:01:24
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{

}



