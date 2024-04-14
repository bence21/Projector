package projector.controller.listener;

import com.bence.projector.common.dto.LoginDTO;

public interface LoginListener {
    void onLogin(LoginDTO loginDTO);
}
