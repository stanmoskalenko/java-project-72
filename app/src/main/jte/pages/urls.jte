@import hexlet.code.dto.UrlsPage
@import hexlet.code.utils.NamedRoutes
@param UrlsPage page


@template.layout.page(
content = @`
    <section>
        <div class="container-lg mt-5">
            <h1>Сайты</h1>

            <table class="table table-bordered table-hover mt-3">
                <thead>
                <tr>
                    <th class="col-1">ID</th>
                    <th>Имя</th>
                    <th class="col-2">Последняя проверка</th>
                    <th class="col-1">Код ответа</th>
                </tr>
                </thead>
                <tbody>
                @for(var url : page.getUrls())
                    <tr>
                        <td>${url.getId().toString()}</td>
                        <td>
                            <a href="${NamedRoutes.urlPath(url.getId().toString())}">${url.getName()}</a>
                        </td>
                        <td>${url.getCreatedAt().toString()}</td>
                        <td></td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </section>
`
)