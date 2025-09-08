import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateFonctionComponent } from './update-fonction.component';

describe('UpdateFonctionComponent', () => {
  let component: UpdateFonctionComponent;
  let fixture: ComponentFixture<UpdateFonctionComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UpdateFonctionComponent]
    });
    fixture = TestBed.createComponent(UpdateFonctionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
