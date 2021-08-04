package dev.abelab.crms.logic;

import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.jsonwebtoken.*;

import lombok.*;
import dev.abelab.crms.db.entity.User;
import dev.abelab.crms.repository.UserRepository;
import dev.abelab.crms.property.JwtProperty;
import dev.abelab.crms.enums.UserRoleEnum;
import dev.abelab.crms.exception.ErrorCode;
import dev.abelab.crms.exception.BadRequestException;
import dev.abelab.crms.exception.UnauthorizedException;
import dev.abelab.crms.exception.ForbiddenException;

@RequiredArgsConstructor
@Component
public class UserLogic {

    private final UserRepository userRepository;

    private final JwtProperty jwtProperty;

    private final PasswordEncoder passwordEncoder;

    /**
     * 管理者チェック
     *
     * @param userId ユーザID
     */
    public void checkAdmin(final int userId) {
        final var user = this.userRepository.selectById(userId);

        if (user.getRoleId() != UserRoleEnum.ADMIN.getId()) {
            throw new ForbiddenException(ErrorCode.USER_HAS_NO_PERMISSION);
        }
    }

    /**
     * JWTを発行
     *
     * @param user ユーザ
     *
     * @return JWT
     */
    public String generateJwt(final User user) {
        final var claims = Jwts.claims();
        claims.put(Claims.ISSUER, this.jwtProperty.getIssuer());
        claims.put("id", user.getId());

        return Jwts.builder() //
            .setClaims(claims) //
            .setIssuer(this.jwtProperty.getIssuer()) //
            .setIssuedAt(new Date()) //
            .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 1 week
            .signWith(SignatureAlgorithm.HS512, this.jwtProperty.getSecret().getBytes()) //
            .compact();
    }

    /**
     * ログインユーザを取得
     *
     * @param jwt JWT
     *
     * @return ユーザ
     */
    public User getLoginUser(final String jwt) {
        // JWTの有効性を検証
        try {
            Jwts.parser().setSigningKey(this.jwtProperty.getSecret().getBytes()).parseClaimsJws(jwt).getBody();
        } catch (SignatureException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_TOKEN);
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_TOKEN);
        } catch (UnsupportedJwtException ex) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_TOKEN);
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }

        final var claim = Jwts.parser().setSigningKey(this.jwtProperty.getSecret().getBytes()).parseClaimsJws(jwt).getBody();
        final var userId = claim.get("id");

        // 無効なJWT
        if (userId == null) {
            throw new UnauthorizedException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        return this.userRepository.selectById((int) userId);
    }

    /**
     * パスワードをハッシュ化
     *
     * @param password パスワード
     *
     * @return ハッシュ値
     */
    public String encodePassword(final String password) {
        return this.passwordEncoder.encode(password);
    }

    /**
     * パスワードが一致するか検証
     *
     * @param user     ユーザ
     *
     * @param password パスワード
     */
    public void verifyPassword(final User user, final String password) {
        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException(ErrorCode.WRONG_PASSWORD);
        }
    }

    /**
     * パスワードが有効かチェック
     */
    public void validatePassword(final String password) {
        // 8文字以上
        if (password.length() < 8) {
            throw new BadRequestException(ErrorCode.TOO_SHORT_PASSWORD);
        }
        // 大文字・小文字・数字を含むか
        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).+$")) {
            throw new BadRequestException(ErrorCode.TOO_SIMPLE_PASSWORD);
        }
    }

}
