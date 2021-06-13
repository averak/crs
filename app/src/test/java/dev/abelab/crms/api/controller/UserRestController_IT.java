package dev.abelab.crms.api.controller;

import static java.lang.String.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpStatus;

import dev.abelab.crms.db.entity.UserSample;
import dev.abelab.crms.repository.UserRepository;
import dev.abelab.crms.enums.UserRoleEnum;
import dev.abelab.crms.api.request.UserCreateRequest;
import dev.abelab.crms.api.response.UserResponse;
import dev.abelab.crms.api.response.UsersResponse;
import dev.abelab.crms.exception.ErrorCode;
import dev.abelab.crms.exception.ConflictException;
import dev.abelab.crms.exception.NotFoundException;

/**
 * UserRestController Integration Test
 */
public class UserRestController_IT extends AbstractRestController_IT {

	// API PATH
	static final String BASE_PATH = "/api/users";
	static final String GET_USERS_PATH = BASE_PATH;
	static final String CREATE_USER_PATH = BASE_PATH;
	static final String UPDATE_USER_PATH = BASE_PATH + "/%d";

	// API Request Body
	static final UserCreateRequest CREATE_USER_BODY = UserCreateRequest.builder() //
		.firstName(SAMPLE_STR) //
		.lastName(SAMPLE_STR) //
		.password(SAMPLE_STR) //
		.email(SAMPLE_STR) //
		.roleId(UserRoleEnum.MEMBER.getId()) //
		.build();

	@Autowired
	UserRepository userRepository;

	/**
	 * ユーザ一覧取得APIのテスト
	 */
	@Nested
	@TestInstance(PER_CLASS)
	class GetUsersTest {

		@Test
		void 正_管理者がユーザ一覧を取得() throws Exception {
			// setup
			final var user1 = UserSample.builder().id(1).email("email1").build();
			final var user2 = UserSample.builder().id(2).email("email2").build();
			userRepository.insert(user1);
			userRepository.insert(user2);

			// send request
			final var request = getRequest(GET_USERS_PATH);
			final var response = execute(request, HttpStatus.OK, UsersResponse.class);

			// verify
			assertThat(response.getUsers()) //
				.extracting(UserResponse::getId) //
				.containsExactly(user1.getId(), user2.getId());
		}

		@Test
		void 異_管理者以外はユーザ一覧を取得不可() throws Exception {
			// FIXME
		}

	}

	/**
	 * ユーザ作成APIのテスト
	 */
	@Nested
	@TestInstance(PER_CLASS)
	class CreateUserTest {

		@Test
		void 正_管理者がユーザを作成() throws Exception {
			// send request
			final var request = postRequest(CREATE_USER_PATH, CREATE_USER_BODY);
			execute(request, HttpStatus.CREATED);

			// verify
			final var createdUser = userRepository.selectByEmail(CREATE_USER_BODY.getEmail());
			assertThat(createdUser.getFirstName()).isEqualTo(CREATE_USER_BODY.getFirstName());
			assertThat(createdUser.getLastName()).isEqualTo(CREATE_USER_BODY.getLastName());
			assertThat(createdUser.getPassword()).isEqualTo(CREATE_USER_BODY.getPassword());
			assertThat(createdUser.getEmail()).isEqualTo(CREATE_USER_BODY.getEmail());
			assertThat(createdUser.getRoleId()).isEqualTo(CREATE_USER_BODY.getRoleId());
			assertThat(createdUser.getDeleted()).isEqualTo(false);
		}

		@Test
		void 異_管理者以外はユーザを作成不可() throws Exception {
			// FIXME
		}

		@Test
		void 異_メールアドレスが既に存在する() throws Exception {
			// send request
			final var request = postRequest(CREATE_USER_PATH, CREATE_USER_BODY);
			execute(request, HttpStatus.CREATED);

			// verify
			execute(request, new ConflictException(ErrorCode.CONFLICT_EMAIL));
		}

		@Test
		void 異_無効なロールを付与() throws Exception {
			// setup
			final var requestBody = UserCreateRequest.builder() //
				.firstName(SAMPLE_STR) //
				.lastName(SAMPLE_STR) //
				.password(SAMPLE_STR) //
				.email(SAMPLE_STR) //
				.roleId(0) //
				.build();

			// verify
			final var request = postRequest(CREATE_USER_PATH, requestBody);
			execute(request, new NotFoundException(ErrorCode.NOT_FOUND_ROLE));
		}

	}

	/**
	 * ユーザ更新APIのテスト
	 */
	@Nested
	@TestInstance(PER_CLASS)
	class UpdateUserTest {

		@Test
		void 正_管理者がユーザを更新() {
			// FIXME
		}

		@Test
		void 異_管理者以外はユーザを更新不可() {
			// FIXME
		}

		@Test
		void 異_更新対象ユーザが存在しない() {
			// FIXME
		}

	}

	/**
	 * ユーザ削除APIのテスト
	 */
	@Nested
	@TestInstance(PER_CLASS)
	class DeleteUserTest {

		@Test
		void 正_管理者がユーザを削除() {
			// FIXME
		}

		@Test
		void 異_管理者以外はユーザを削除不可() {
			// FIXME
		}

		@Test
		void 異_削除対象ユーザが存在しない() {
			// FIXME
		}

	}

}
