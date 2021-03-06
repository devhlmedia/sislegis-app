package br.gov.mj.sislegis.app.rest.serializers;

import java.io.IOException;

import br.gov.mj.sislegis.app.model.RoadmapComissao;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CompactRoadmapComissaoSerializer extends JsonSerializer<RoadmapComissao> {

    @Override
    public void serialize(RoadmapComissao roadmapComissao, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("comissao", roadmapComissao.getComissao());
        jsonGenerator.writeEndObject();
    }
}
