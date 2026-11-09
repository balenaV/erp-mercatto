# Mercatto ERP

Aplicação desktop acadêmica em Java e JavaFX. Produtos, clientes, carrinho e vendas são mantidos em coleções na memória durante a execução. O aplicativo começa vazio. Ao fechá-lo, todos os registros são descartados; a próxima execução começa novamente vazia.

## Executar no Windows

Requisito para desenvolver: **JDK 21 ou superior** e acesso à internet na primeira compilação para obter o Maven e as dependências. O código é compilado para Java 21 e usa JavaFX 21.0.13.

No PowerShell, dentro desta pasta:

```powershell
.\iniciar.ps1
```

O script procura o JDK em `JAVA_HOME`, no `PATH` e em `%USERPROFILE%\.jdks`, sem alterar configurações permanentes. Também é possível executar com um JDK configurado:

```powershell
.\mvnw.cmd compile javafx:run
```

No IntelliJ, abra **o arquivo `pom.xml` desta pasta como projeto Maven**, selecione um JDK 21+ em Project SDK e aguarde a importação. Execute `br.com.mercatto.main.Launcher`, ou a tarefa Maven `javafx:run`. A classe JavaFX principal é `br.com.mercatto.main.Main`; `Launcher` permite também executar o JAR e o aplicativo empacotado. A pasta `demo` não é a raiz deste projeto.

## Fluxo de utilização

1. Em **Produtos → Novo produto**, apenas **descrição e valor de venda positivo** são obrigatórios. O código é opcional e, se ficar em branco, será gerado automaticamente sem duplicar códigos existentes. O estoque também é opcional e começa em zero quando não informado; a situação inicial é Ativo. Para vender, informe estoque disponível. É possível buscar, editar, inativar e excluir registros sem vendas associadas.
2. Em **Clientes → Novo cliente**, informe nome, CPF/CNPJ, telefone com DDD e situação. A identificação do cliente na venda é opcional.
3. Selecione um produto e clique em **Adicionar ao PDV**, ou busque pelo código/descrição no PDV. Escolha nas sugestões quando houver mais de um resultado. Se a busca não encontrar um produto, clique em **Criar novo produto** para abrir o mesmo formulário do gerenciador, com a descrição pesquisada preenchida. Ao salvar, o produto fica selecionado na busca, sem alterar o carrinho até você adicioná-lo. Cancelar preserva a busca e a venda em andamento. Quando só houver produtos inativos, a tela orienta sua ativação no gerenciador. Informe a quantidade e clique em **Adicionar** ou pressione Enter. O mesmo produto é agrupado no carrinho.
4. Para mudar a quantidade, selecione o item e clique em **Quantidade** ou dê duplo clique na linha. É possível remover um item, limpar os itens ou cancelar toda a venda. A troca de módulo preserva o carrinho atual.
5. Selecione um cliente no resumo, ou use **Usar no PDV** na consulta de clientes. A opção **Consumidor não identificado** remove a identificação.
6. Em **Ir para pagamento**, escolha Dinheiro, Cartão ou PIX. Em dinheiro, informe um valor suficiente; o troco é calculado. Cartão e PIX registram o total integral após a confirmação do operador. Não há processamento externo de pagamentos.
7. Confirme o pagamento. Os produtos e o cliente são revalidados, o estoque é baixado e uma venda é registrada. Uma falha de validação mantém estoque, carrinho e histórico intactos.
8. Em **Vendas realizadas**, filtre por período e/ou cliente, selecione uma venda e abra **Ver detalhes**. As datas inicial e final são inclusivas. **Limpar** restaura a consulta completa.

Produtos e clientes inativos não podem participar de novas vendas. Registros usados por vendas concluídas podem ser inativados, mas não excluídos. Um produto presente no carrinho deve ser removido antes da exclusão.

O preço unitário é fixado quando o produto entra no carrinho; alterações posteriores no catálogo não alteram essa cotação. As vendas concluídas guardam cópias dos nomes, documentos, códigos e preços, preservando o histórico durante a execução. Não há alteração ou cancelamento de vendas já concluídas nesta versão.

CPF/CNPJ e telefone têm validação de **formato e unicidade**, sem consulta externa ou verificação de titularidade/dígitos verificadores. CPF exige 11 dígitos; CNPJ exige 14 caracteres, com os dois últimos numéricos. O telefone exige DDD e 10 ou 11 dígitos.

## Scene Builder

Os nove arquivos em `src/main/resources/br/com/mercatto/view` continuam editáveis individualmente no Scene Builder. Abra o próprio `.fxml`; a folha compartilhada está em `../css/mercatto.css`.

Agora cada arquivo também declara `fx:controller` e eventos para executar a aplicação. O Scene Builder permite editar os layouts sem executar esses controllers. Os campos vazios e as tabelas sem registros no editor são intencionais. O valor inicial da quantidade no PDV é configurado pelo controller ao executar.

Não mova os FXML isoladamente: preserve a relação entre as pastas `view` e `css`. Mantenha os identificadores `fx:id` e os nomes dos eventos ao editar. Os grupos de situação e pagamento estão declarados dentro do primeiro controle de cada grupo, para compatibilidade com o editor.

## Organização e POO

O projeto segue uma estrutura MVC simples, baseada no exemplo `ExemploJavaFx`: os controllers recebem eventos das telas e chamam os DAOs, que mantêm os objetos em listas na memória. Cada DAO cuida do seu cadastro ou da operação de venda, incluindo as validações necessárias.

```text
src/main/java/br/com/mercatto/
  main/Main.java                 Inicialização JavaFX com FXMLLoader padrão
  main/Launcher.java             Entrada para execução do JAR e do aplicativo Windows
  model/                         Classes dos dados e formas de pagamento
  model/dao/                     ProdutoDAO, ClienteDAO e VendaDAO, com listas em memória
  model/dao/DadosAplicacao.java   Reúne os três DAOs e o carrinho desta execução
  controller/                    Eventos das nove telas
  util/                          Formatação, tabelas e diálogos
src/main/resources/br/com/mercatto/
  view/                          Nove telas FXML
  css/mercatto.css                Tema visual compartilhado
src/test/java/br/com/mercatto/    Testes de regras, valores e fluxo de interface
docs/DOCUMENTACAO.md             Documento de entrega (integrantes, classes, POO, fluxo)
docs/POO.md                      Explicação para apresentação acadêmica
docs/ARQUITETURA.md              Relação com o exemplo e passagem de dados entre telas
output/pdf/                      Documento de requisitos já revisado
```

As entidades têm atributos privados, construtores, getters e operações que validam os valores. `Main` cria uma única instância de `DadosAplicacao` e a entrega ao `PrincipalController`. O principal carrega as outras telas com `FXMLLoader.load()`, obtém o controller com `getController()` e chama `setPrincipal(this)`. Cada controller recebe assim os mesmos DAOs e o mesmo carrinho. Não há fábrica de controllers ou camada de serviços.

Os controllers possuem construtor padrão, como no exemplo. A navegação e a abertura dos modais estão concentradas no `PrincipalController`, com métodos explícitos para cada tela. O produto selecionado na consulta é a mesma instância usada no carrinho; não é necessário recadastrar ou redigitar suas informações.

`Pagamento` é uma classe abstrata, especializada por `PagamentoDinheiro`, `PagamentoCartao` e `PagamentoPix`. A venda recebe uma referência do tipo `Pagamento` e usa o comportamento de validação/troco da implementação concreta. Veja `docs/POO.md` para o detalhamento.

## Verificar e empacotar

```powershell
.\mvnw.cmd verify
.\empacotar.ps1
```

Os testes de interface abrem janelas temporárias usando o toolkit JavaFX real; execute-os em uma sessão gráfica. Os únicos registros de exemplo pertencem aos testes e não são carregados pelo aplicativo.

O empacotamento gera uma pasta em `dist/<data-hora>/Mercatto` com **Mercatto.exe e um runtime Java incluído**. Distribua a pasta inteira; o `.exe` sozinho não contém as bibliotecas. Essa cópia executável dispensa a instalação separada do Java e não instala serviços no Windows. O JDK usado para empacotar determina a versão do runtime incluído.

Também é possível usar `target/mercatto.jar` junto da pasta `target/lib`, executando `java -jar target/mercatto.jar` com um Java compatível. Não separe o JAR de suas dependências.
