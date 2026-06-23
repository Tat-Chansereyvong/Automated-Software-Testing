import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Enable global validation so invalid DTOs return 400
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,            // strip unknown properties
      forbidNonWhitelisted: true, // throw if unknown properties are sent
      transform: true,            // auto-transform payloads to DTO instances
    }),
  );

  await app.listen(3000);
  console.log('🚀 Server running on http://localhost:3000');
}
bootstrap();
