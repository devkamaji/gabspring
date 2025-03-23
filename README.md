# 📚 Projeto GabSpring

## 📝 Descrição
Este projeto é um exemplo de como criar um framework web inspirado no Spring do zero. Ele inclui funcionalidades básicas como injeção de dependências, mapeamento de rotas e um contêiner web embutido.

## 🚀 Tecnologias Utilizadas
- **Java**
- **Maven**
- **Tomcat Embutido**
- **Gson**

## 📂 Estrutura do Projeto
- `src/main/java/br/com/gabspring/`: Contém o código do framework GabSpring.
    - `annotations/`: Anotações personalizadas para mapeamento de rotas e injeção de dependências.
    - `datastructures/`: Estruturas de dados utilizadas pelo framework.
    - `explorer/`: Classes para exploração de metadados.
    - `util/`: Utilitários como o logger.
    - `web/`: Classes relacionadas ao contêiner web e ao despachante de requisições.

## 📦 Funcionalidades
- **Injeção de Dependências**: Utiliza anotações para injetar dependências automaticamente.
- **Mapeamento de Rotas**: Mapeia métodos de controladores para rotas HTTP usando anotações.
- **Contêiner Web Embutido**: Utiliza Tomcat embutido para servir a aplicação.

## 🛠️ Como Funciona
1. **Anotações**: Utilize anotações como `@GabController`, `@GabService`, `@GabGetMethod`, `@GabPostMethod` e `@GabInjected` para definir controladores, serviços, métodos de requisição e injeção de dependências.
2. **Exploração de Classes**: O `ClassExplorer` encontra todas as classes anotadas e registra suas informações.
3. **Despachante de Requisições**: O `GabSpringDispatchServlet` lida com as requisições HTTP, invocando os métodos apropriados dos controladores.

## 📚 Exemplo de Uso
### Controlador
```java
@GabController
public class TestController {
    @GabInjected
    private IServiceExample serviceExample;

    @GabGetMethod(path = "/test")
    public String test() {
        return "Hello, World!";
    }

    @GabGetMethod(path = "/service")
    public String getServiceMessage() {
        return serviceExample.getMessage();
    }
}
```

### Serviço
```java
@GabService
public class ServiceExample implements IServiceExample {
    @Override
    public String getMessage() {
        return "Hello from ServiceExample!";
    }
}
```

## 🎉 Conclusão
Este projeto é uma ótima maneira de entender os conceitos fundamentais por trás de um framework web como o Spring. Divirta-se explorando e aprendendo!