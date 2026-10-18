# Libris Mobile — visual refresh

**Data:** 2026-10-17  
**Status:** aprovado pela Jess  
**Direção escolhida:** “Arquivo calmo + fita vinho” (opção A)

## Intenção

Levar para Android e iOS a identidade “arquivo pessoal” já aprovada no Libris para Mac sem encolher literalmente o layout desktop. O app deve parecer parte da mesma família: mesa de livro-razão, fichas de papel, tinta vinho e pequenos detalhes de latão. No telefone, hierarquia, densidade e alvos de toque têm prioridade sobre a reprodução literal de elementos físicos.

## Objetivos

- Aplicar a identidade visual aprovada a todas as telas do `composeApp`.
- Reduzir o ruído visual atual, especialmente nos filtros, chips de gênero e barra inferior.
- Manter boa leitura e uso com uma mão em telas estreitas.
- Usar os mesmos componentes e tokens em Android e iOS.
- Preservar integralmente os fluxos, ViewModels, API, navegação e regras de negócio existentes.

## Fora de escopo

- Alterações de API, autenticação, persistência, rotas ou domínio.
- Novas funcionalidades, suporte offline ou layout específico para tablet.
- Redesenho dos ícones funcionais além do necessário para harmonizar cor e tamanho.
- Animações decorativas ou uma migração para componentes Material com aparência padrão.

## Sistema visual

### Cores

Usar os tokens já presentes em `Theme.kt` e iguais aos do Mac:

| Token | Cor | Papel |
|---|---|---|
| `desk` | `#292A1E` | mesa verde quase preta |
| `ledgerLine` | `#9A8A59` | linhas discretas do livro-razão |
| `paper` | `#F2E5C9` | fichas e folhas |
| `paperShade` | `#D8C39E` | dobras e profundidade do papel |
| `ink` | `#30231B` | texto principal sobre papel |
| `inkSoft` | `#6C5946` | metadados e texto secundário |
| `oxblood` | `#741B18` | identidade, ação principal e seleção |
| `brass` | `#B6873F` | foco, detalhe e seleção secundária |

O vinho deve ser o único destaque forte recorrente. Latão aparece pouco. Gêneros são neutros e não recebem cores alternadas.

### Tipografia

- Aleo para títulos de página, títulos de livros e carimbos expressivos.
- Yantramanav para navegação, campos, metadados e texto corrido.
- Títulos de página com peso 700 e espaçamento levemente fechado.
- Rótulos de navegação em sentence case; caixa alta fica restrita aos filtros de situação, onde funciona como linguagem de carimbo.

### Mesa e papel

- O fundo continua sendo o livro-razão, mas ampliado e com contraste baixo como no Mac.
- Fichas usam um gradiente curto de papel, sombra deslocada curta e uma dobra no canto superior direito.
- Folhas longas de detalhe/formulário não devem ser fragmentadas em vários cards Material arredondados.

## Estrutura global

### Barra inferior

A navegação principal é uma fita de tecido vinho contínua no rodapé:

- quatro destinos: Acervo, Wishlist, Números e Registrar;
- textura muito discreta feita por desenho/gradiente, sem bitmap adicional;
- ícones e texto em papel envelhecido;
- item ativo com maior opacidade e linha curta de latão;
- respeita `navigationBarsPadding` e alvos mínimos de 48 dp;
- “Sair” deixa a barra e vira ação discreta no topo das telas principais.

### Margens e largura

- Conteúdo principal com 18–20 dp de margem lateral.
- Listas com 12–14 dp entre fichas.
- Componentes devem funcionar a partir de 320 dp de largura sem cortar ações essenciais.
- Safe areas de Android e iOS continuam respeitadas.

## Telas

### Login

Placa ex-libris de papel centralizada sobre a mesa. Nome do app em Aleo vinho, subtítulo curto, campos lineares e botão principal vinho. Nenhuma barra inferior antes de autenticar.

### Acervo e Wishlist

- Cabeçalho direto sobre a mesa: título, subtítulo/contagem e ação discreta de sessão.
- Filtros de situação em uma linha horizontal rolável e tipográfica. Só o ativo recebe cor clara e sublinhado de latão; os outros ficam em `deskTextSoft`.
- Gênero e busca em campos escuros com borda de baixo contraste.
- Livros em lista de uma coluna com capa, título, autores, progresso e gêneros.
- Cards ganham dobra de papel; gêneros viram texto neutro sublinhado.
- Estado vazio usa uma ficha de papel com uma ação clara. Loading e erro permanecem diretamente legíveis sobre a mesa.

### Ficha

Uma folha principal contínua em uma coluna:

1. capa, título, autoria e gêneros;
2. situação, progresso e dados de leitura;
3. ação principal de registrar leitura;
4. histórico de leituras;
5. ações destrutivas visualmente separadas e sem competir com a ação principal.

O carimbo físico completo continua fazendo sentido dentro da ficha. Edição e voltar ficam no topo da folha.

### Registrar e editar

- Formulário organizado em seções sobre uma folha longa, sem caixas arredondadas genéricas.
- Campos lineares com rótulos claros.
- ISBN, scanner e seletor de capa tratados como ferramentas secundárias em latão/tinta.
- Gêneros escolhidos e sugeridos usam etiquetas neutras; sugestões podem usar linha tracejada de latão.
- Ação de salvar em vinho e sempre alcançável após o conteúdo; nenhuma mudança na validação atual.

### Gerenciar gêneros

Lista de índice sobre papel, com criação no topo e edição/remoção por linha. A remoção continua exigindo a confirmação existente.

### Números

Uma folha longa com números de destaque e gráficos em tinta, vinho e latão. Divisórias e alinhamento fazem a estrutura; evitar uma grade de cards Material independentes.

## Estados e interação

- Feedback de toque pode usar mudança curta de opacidade; não adicionar movimento automático.
- Foco, seleção e ações precisam ser distinguíveis sem depender só da cor.
- Preservar descrições semânticas, papéis de tab e conteúdo mínimo de 48 dp.
- Respeitar fonte ampliada sem sobrepor navegação ou cortar títulos críticos.
- O refresh visual cobre estados carregando, vazio, erro, conteúdo e confirmação.

## Chamadas do implementador

- Ajustes finos de espaçamento, tamanhos e quebra de linha podem ser decididos durante a validação nos simuladores.
- A implementação pode extrair componentes/tokens reutilizáveis quando reduzir repetição real.
- O ícone de sessão no topo pode usar o ícone `Exit` existente, com descrição acessível “Sair”.

## Critérios de conclusão

- Todas as telas autenticadas usam a fita vinho com quatro destinos e ação de sair fora dela.
- Acervo/Wishlist, Ficha, formulário, gêneros, números e login seguem o sistema acima.
- Não restam chips multicoloridos nem a barra inferior clara anterior.
- Android e iOS compilam; testes comuns continuam passando.
- A tela do Acervo é validada visualmente no Pixel e ao menos uma tela longa é conferida em tamanho estreito.
