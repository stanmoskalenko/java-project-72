@import hexlet.code.dto.UrlPage
@param UrlPage page


@template.layout.page(
page = page,
content = @`
    <section>
        <div class="container-lg mt-5">
            <h1>Сайт: ${page.getName()}</h1>

            <table class="table table-bordered table-hover mt-3">
                <tbody>
                <tr>
                    <td>ID</td>
                    <td>${page.getId().toString()}</td>
                </tr>
                <tr>
                    <td>Имя</td>
                    <td>${page.getName()}</td>
                </tr>
                <tr>
                    <td>Дата создания</td>
                    <td>${page.getCreatedAt()}</td>
                </tr>
                </tbody>
            </table>

            <h2 class="mt-5">Проверки</h2>
            <form method="post" action="/urls/${page.getId()}/checks">
                <button type="submit" class="btn btn-primary">Запустить проверку</button>
            </form>

            <table class="table table-bordered table-hover mt-3">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Код ответа</th>
                    <th>title</th>
                    <th>h1</th>
                    <th>description</th>
                    <th>Дата проверки</th>
                </tr>
                </thead>
                <tbody>
                @if(page.getChecks() != null)
                    @for(var url : page.getChecks())
                        <tr>
                            <td>${url.getId().toString()}</td>
                            <td>${url.getStatusCode()}</td>
                            <td>${url.getTitle()}</td>
                            <td>${url.getH1()}</td>
                            <td>${url.getDescription()}</td>
                            <td>${url.getCreatedAt()}</td>
                        </tr>
                    @endfor
                @endif
                </tbody>
            </table>
        </div>
    </section>
`
)