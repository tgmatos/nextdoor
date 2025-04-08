# NextDoor

To start your Phoenix server:

  * Run `mix setup` to install and setup dependencies
  * Start Phoenix endpoint with `mix phx.server` or inside IEx with `iex -S mix phx.server`

Now you can visit [`localhost:4000`](http://localhost:4000) from your browser.

Ready to run in production? Please [check our deployment guides](https://hexdocs.pm/phoenix/deployment.html).

# Regras de Negócio

## 1. Cadastro e Gerenciamento de Lojas
- **Cadastro de Lojas**: As lojas devem se cadastrar na plataforma, fornecendo informações como nome, endereço, telefone, e-mail e categoria.
- **Validação de Cadastro**: A plataforma deve validar as informações fornecidas antes de aprovar o cadastro da loja.
- **Gerenciamento de Perfil**: As lojas devem ter a capacidade de editar suas informações, como horários de funcionamento, descrição e fotos.

## 2. Gerenciamento de Produtos
- **Cadastro de Produtos**: As lojas devem poder cadastrar produtos, incluindo nome, descrição, preço, categoria e imagens.
- **Controle de Estoque**: As lojas devem gerenciar o estoque de produtos, atualizando a disponibilidade em tempo real.
- **Promoções e Descontos**: As lojas podem criar promoções e aplicar descontos em produtos ou categorias específicas.

## 3. Processamento de Pedidos
- **Recebimento de Pedidos**: As lojas devem receber notificações de novos pedidos em tempo real.
- **Status do Pedido**: As lojas devem atualizar o status do pedido (ex: em preparação, pronto para entrega, entregue).
- **Cancelamento de Pedidos**: As lojas devem ter a opção de cancelar pedidos sob certas condições (ex: falta de estoque).

## 4. Entrega
- **Opções de Entrega**: As lojas podem oferecer diferentes opções de entrega (ex: entrega própria, entrega por terceiros).
- **Taxas de Entrega**: As lojas devem definir suas próprias taxas de entrega, se aplicável.
- **Tempo de Entrega**: As lojas devem informar o tempo estimado de entrega para os clientes.

## 5. Pagamentos
- **Métodos de Pagamento**: As lojas devem aceitar diferentes métodos de pagamento (ex: cartão de crédito, débito, dinheiro).
- **Comissões**: A plataforma pode cobrar uma comissão sobre as vendas realizadas pelas lojas, que deve ser claramente informada.
- **Relatórios de Vendas**: As lojas devem ter acesso a relatórios de vendas e comissões.

## 6. Atendimento ao Cliente
- **Suporte ao Cliente**: As lojas devem oferecer suporte ao cliente para resolver dúvidas e problemas relacionados a pedidos.
- **Avaliações e Feedback**: As lojas devem poder responder a avaliações e feedbacks deixados pelos clientes.

## 7. Conformidade e Segurança
- **Políticas de Privacidade**: As lojas devem seguir as políticas de privacidade e proteção de dados dos clientes.
- **Conformidade Legal**: As lojas devem estar em conformidade com as leis locais e regulamentos relacionados a comércio e entrega.

## 8. Relacionamento com a Plataforma
- **Taxas e Pagamentos**: As lojas devem estar cientes das taxas cobradas pela plataforma e dos prazos de pagamento.
- **Comunicação**: As lojas devem receber atualizações e comunicados da plataforma sobre mudanças, novas funcionalidades e políticas.
