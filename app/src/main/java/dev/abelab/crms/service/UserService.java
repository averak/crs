package dev.abelab.crms.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.*;
import dev.abelab.crms.db.entity.User;
import dev.abelab.crms.api.response.UserResponse;
import dev.abelab.crms.api.response.UsersResponse;
import dev.abelab.crms.api.request.UserCreateRequest;
import dev.abelab.crms.logic.UserLogic;
import dev.abelab.crms.logic.UserRoleLogic;
import dev.abelab.crms.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserLogic userLogic;

    private final UserRoleLogic userRoleLogic;

    private final UserRepository userRepository;

    /**
     * ユーザ一覧を取得
     *
     * @return ユーザ一覧レスポンス
     */
    @Transactional
    public UsersResponse getUsers() {
        // FIXME: 権限チェック
        // this.userLogic.checkAdmin(userId);

        // ユーザの取得
        final var users = userRepository.findAll();
        final var userResponses = users.stream().map(user -> {
            return UserResponse.builder() //
                .id(user.getId()) //
                .firstName(user.getFirstName()) //
                .lastName(user.getLastName()) //
                .email(user.getEmail()) //
                .roleId(user.getRoleId()) //
                .deleted(user.getDeleted()) //
                .build();
        }).collect(Collectors.toList());

        return new UsersResponse(userResponses);
    }

    /**
     * ユーザを作成
     *
     * @param userRequest ユーザ作成リクエスト
     */
    @Transactional
    public void createUser(UserCreateRequest userRequest) {
        // 有効なロールかチェック
        this.userRoleLogic.checkForValidRoleId(userRequest.getRoleId());

        // ユーザの作成
        final var user = User.builder() //
            .firstName(userRequest.getFirstName()) //
            .lastName(userRequest.getLastName()) //
            .email(userRequest.getEmail()) //
            .password(userRequest.getPassword()) //
            .roleId(userRequest.getRoleId()) //
            .deleted(false) //
            .build();

        this.userRepository.insert(user);
    }

}
