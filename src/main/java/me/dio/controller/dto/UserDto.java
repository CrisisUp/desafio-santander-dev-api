package me.dio.controller.dto;

import me.dio.domain.model.User;
import me.dio.domain.model.Card;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public record UserDto(
        Long id,
        String name,
        AccountDto account,
        CardDto card,
        List<FeatureDto> features,
        List<NewsDto> news) {

    public UserDto(User model) {
        this(
                model.getId(),
                model.getName(),
                ofNullable(model.getAccount()).map(AccountDto::new).orElse(null),
                ofNullable(model.getCard()).map(CardDto::new).orElse(null),
                ofNullable(model.getFeatures()).orElse(emptyList()).stream().map(FeatureDto::new).collect(toList()),
                ofNullable(model.getNews()).orElse(emptyList()).stream().map(NewsDto::new).collect(toList())
        );
    }

    public User toModel() {
        User model = new User();
        model.setId(this.id);
        model.setName(this.name);
        model.setAccount(ofNullable(this.account).map(AccountDto::toModel).orElse(null));
        model.setCard(ofNullable(this.card).map(CardDto::toModel).orElse(null));
        // Lists pass through null so the service can distinguish "field absent"
        // from "empty list" (the update validation rejects a null features/news).
        model.setFeatures(ofNullable(this.features).map(feats -> feats.stream().map(FeatureDto::toModel).collect(toList())).orElse(null));
        model.setNews(ofNullable(this.news).map(newsItems -> newsItems.stream().map(NewsDto::toModel).collect(toList())).orElse(null));
        return model;
    }

}

