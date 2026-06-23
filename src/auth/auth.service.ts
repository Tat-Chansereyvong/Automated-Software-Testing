import { Injectable, UnauthorizedException } from '@nestjs/common';
import * as crypto from 'crypto';

// Hardcoded user for demo purposes
const DEMO_USER = {
  username: 'admin',
  password: 'admin123',
};

@Injectable()
export class AuthService {
  private readonly secret = 'lab-secret-key';

  login(username: string, password: string): { access_token: string } {
    if (username !== DEMO_USER.username || password !== DEMO_USER.password) {
      throw new UnauthorizedException('Invalid credentials');
    }

    // Create a simple token: base64(payload).signature
    const payload = Buffer.from(
      JSON.stringify({ username, iat: Date.now() }),
    ).toString('base64');

    const signature = crypto
      .createHmac('sha256', this.secret)
      .update(payload)
      .digest('hex');

    return { access_token: `${payload}.${signature}` };
  }

  validateToken(token: string): { username: string } {
    try {
      const [payload, signature] = token.split('.');

      // Verify signature
      const expectedSig = crypto
        .createHmac('sha256', this.secret)
        .update(payload)
        .digest('hex');

      if (signature !== expectedSig) {
        throw new UnauthorizedException('Invalid token');
      }

      const decoded = JSON.parse(Buffer.from(payload, 'base64').toString());
      return { username: decoded.username };
    } catch {
      throw new UnauthorizedException('Invalid token');
    }
  }
}
