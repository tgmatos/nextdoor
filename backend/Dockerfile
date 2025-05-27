FROM elixir:1.18.4

WORKDIR /app

COPY mix.exs mix.lock ./

RUN mix local.hex --force && \
    mix local.rebar --force

RUN mix deps.get

COPY . .

RUN mix compile

CMD ["mix", "phx.server", "--no-halt"]